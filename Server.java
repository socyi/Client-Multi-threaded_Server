import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.json.JSONException;
import org.json.JSONObject;

public class Server {

	private static class TCPWorker implements Runnable {

		private Socket client;
		private String clientbuffer;

		public TCPWorker(Socket client) {
			this.client = client;
			this.clientbuffer = "";
		}

		@Override
		public void run() {
			String response = "";
			try {
				System.out.println("Client connected with: " + this.client.getInetAddress());
				Connection dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/phpmyadmin",
						"sgiann05", "esxm1111!!!!");
				DataOutputStream output = new DataOutputStream(client.getOutputStream());
				BufferedReader input = new BufferedReader(new InputStreamReader(this.client.getInputStream()));

				while (true) {
					this.clientbuffer = input.readLine();
					System.out.println("[" + new Date() + "] Received: " + this.clientbuffer);
					JSONObject json = new JSONObject(this.clientbuffer);
					String option = json.getString("type");
					String query;
					ResultSet rs;
					PreparedStatement preparedStmt;
					Statement stmt;
					switch (option) {
					case "insert":
						
						query = "insert into students (firstname, lastname, username, average) values (?,?,?,?)";
						preparedStmt = dbConnection.prepareStatement(query);
						preparedStmt.setString(1, json.getString("firstname"));
						preparedStmt.setString(2, json.getString("lastname"));
						preparedStmt.setString(3, json.getString("username"));
						preparedStmt.setString(4, json.getString("average"));
						preparedStmt.execute();
						response = "{\"status\": \"success\", \"action\": \"tuple inserted to db\"}\n";
						output.writeBytes(response);
						break;
					case "select":
						
						query = "select * from students";
						stmt = dbConnection.createStatement();
						rs = stmt.executeQuery(query);
						response = "{\"status\": \"success\", \"action\": \"student data retrieved\", \"students\": [";
						int count = 0;
						while (rs.next()) {

							if (count != 0)
								response += ", ";
							response += "{\"id\":" + rs.getString("id") + ", ";
							response += "\"firstname\": \"" + rs.getString("firstname") + "\",";
							response += "\"lastname\": \"" + rs.getString("lastname") + "\",";
							response += "\"username\": \"" + rs.getString("username") + "\",";
							response += "\"average\": " + rs.getString("average") + "}";
							count++;
						}
						
						response += "]}\n";
						output.writeBytes(response);
						break;
					case "delete":
						
						query = "delete from students where id= (?)";
						preparedStmt = dbConnection.prepareStatement(query);
						preparedStmt.setString(1, json.getString("id"));
						preparedStmt.execute();
						response = "{\"status\": \"success\", \"action\": \"student deleted\"}\n";
						output.writeBytes(response);
						break;
					case "disconnect":

						response = "{\"status\": \"success\", \"action\": \"connection closed\"}\n";
						output.writeBytes(response);
						this.client.close();
						break;
					default:
						
						response = "{\"status\": \"error\"}\n";
						output.writeBytes(response);
					}

				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public static ExecutorService TCP_WORKER_SERVICE = Executors.newFixedThreadPool(5);

	public static void main(String args[]) throws SQLException {
		try {
			
			ServerSocket socket = new ServerSocket (Integer.parseInt(args[0]));
			String message, response;
			System.out.println("Server listening to: " + socket.getInetAddress() + ":" + socket.getLocalPort());

			while (true) {
				Socket client = socket.accept();

				TCP_WORKER_SERVICE.submit(new TCPWorker(client));

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
