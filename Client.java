import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.sql.PreparedStatement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Client {
	public static void main(String[] args) {
		try {
			Socket socket = new Socket(args[0],Integer.parseInt(args[1]));

			String message = "", response = "", line = "", inp = "";
			String[] values;

			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			BufferedReader server = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Welcome to Students Registration Service \n"
					+ " Choose one of the following options:\n 1. Insert student.\n "
					+ "2. Show all students.\n 3. Delete student.\n 4. Quit service.\n");

			while (!line.equals("4")) {
				line = input.readLine();
				if (line.equals("1")) {
					System.out.println("Give student's first name, last name, username and average"
							+ " grade separated by comma: ");
					inp = input.readLine();
					values = inp.split(",");
					while (values.length != 4) {
						System.out.println("Wrong input!\n");
						inp = input.readLine();
						values = inp.split(",");
					}

					message = "{\"type\": \"insert\", \"firstname\": \"" + values[0] + "\",\"lastname\": \"" + values[1]
							+ "\"," + " \"username\": \"" + values[2] + "\", \"average\": " + values[3] + "}\n";
					output.writeBytes(message);
					response = server.readLine();
					System.out.println("[" + new Date() + "] Received: " + response + "\n");
					System.out.println("Choose one of the following options:\n 1. Insert student.\n "
							+ "2. Show all students.\n 3. Delete student.\n 4. Quit service.\n");
					
				} else if (line.equals("2")) {
					message = "{\"type\": \"select\"}\n";
					output.writeBytes(message);
					response = server.readLine();
					System.out.println("[" + new Date() + "] Received: " + response + "\n");
					JSONObject json = new JSONObject(response);
					JSONArray json2 = json.getJSONArray("students");
					System.out.print("ID\tFName\t\tLName\t\tUName\t\t\tAverage\t\t\n");
					for (int i = 0; i < json2.length(); i++) {
						System.out.print(json2.getJSONObject(i).get("id") + "\t");
						System.out.print(json2.getJSONObject(i).get("firstname") + "\t\t");
						System.out.print(json2.getJSONObject(i).get("lastname") + "\t\t");
						System.out.print(json2.getJSONObject(i).get("username") + "\t\t\t");
						System.out.print(json2.getJSONObject(i).get("average") + "\t\t");
						System.out.println();
					}

					System.out.println("\nChoose one of the following options:\n 1. Insert student.\n "
							+ "2. Show all students.\n 3. Delete student.\n 4. Quit service.\n");
				} else if (line.equals("3")) {
					System.out.println("Give the id of the student to be deleted:");
					inp = input.readLine();
					message = "{\"type\": \"delete\", \"id\":" + inp + "}\n";
					output.writeBytes(message);
					response = server.readLine();
					System.out.println("[" + new Date() + "] Received: " + response + "\n");
					System.out.println("\nChoose one of the following options:\n 1. Insert student.\n "
							+ "2. Show all students.\n 3. Delete student.\n 4. Quit service.\n");

				} else if (!line.equals("4")){
					System.out.println("Wrong input! \n");
				}

			}
			message = "{\"type\": \"disconnect\"}\n";
			output.writeBytes(message);
			response = server.readLine();
			System.out.println("[" + new Date() + "] Received: " + response + "\n");
			socket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
