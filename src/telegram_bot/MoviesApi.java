package telegram_bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MoviesApi {
	private String apiKey;
	
	public MoviesApi(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public List<String> buscarFilme(String termo) throws IOException {

		
		OkHttpClient client = new OkHttpClient();

		StringBuilder sbUrlComParametros = new StringBuilder("");
		
		String endpoint = "https://online-movie-database.p.rapidapi.com/auto-complete";
		sbUrlComParametros.append(endpoint);
		sbUrlComParametros.append(String.format("?q=%s", termo));
		
		Request request = new Request.Builder()
			.url(sbUrlComParametros.toString())
			.get()
			.addHeader("X-RapidAPI-Host", "online-movie-database.p.rapidapi.com")
			.addHeader("X-RapidAPI-Key", this.apiKey)
			.build();

		Response response = client.newCall(request).execute();
		
		Console.printarComentario(response.toString());
		//response.
		
		return new ArrayList<String>();
	}
}
