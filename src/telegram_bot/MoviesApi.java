package telegram_bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
	
	public List<ComandoBot> retornarListaComandos(String comandoPai) {

		List<ComandoBot> listaComandos = new ArrayList<ComandoBot>();

		listaComandos.add(new ComandoBot("/buscarfilme", "Buscar filme", fBuscarFilme(), comandoPai));
		
		return listaComandos;
	}

	private Function<String, String> fBuscarFilme() {
		return parametro -> {

			List<String> listaFilmes;

			try {
				// listaFilmes = moviesApi.buscarFilme(parametro);
				listaFilmes = buscarFilme("game");

				if (listaFilmes.isEmpty()) {
					return "Nenhum filme encontrado";
				}

				// Inicializa um buffer de strings, para concatenar os filmes encontrados
				StringBuilder stringBuilderComandos = new StringBuilder("Filmes encontrados:");

				// Adiciona cada um dos filmes no buffer de strings
				for (String filme : listaFilmes) {
					stringBuilderComandos.append("\n");
					stringBuilderComandos.append(filme);
				}

				// Retorna string resultante do buffer
				return stringBuilderComandos.toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "Erro ao buscar filme";
			}
		};
	}
}
