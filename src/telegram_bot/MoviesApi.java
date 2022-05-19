package telegram_bot;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import telegram_bot.Models.BuscaFilmeModel;
import telegram_bot.Models.DetalhesFilmeModel;
import telegram_bot.Models.FilmeModel;
import telegram_bot.Models.SinopseFilmeModel;

public class MoviesApi {
	private String apiProtocol;
	private String apiUrl;
	private String apiKey;

	public MoviesApi(String apiKey) {
		this.apiProtocol = "https://";
		this.apiUrl = "online-movie-database.p.rapidapi.com";
		this.apiKey = apiKey;
	}

	public BuscaFilmeModel buscarFilme(String termo) throws IOException {

		try {
			StringBuilder sbUrlComParametros = new StringBuilder("");
			sbUrlComParametros.append(apiProtocol);
			sbUrlComParametros.append(apiUrl);
			sbUrlComParametros.append("/auto-complete");
			sbUrlComParametros.append(String.format("?q=%s", termo));

			Request request = new Request.Builder()
					.url(sbUrlComParametros.toString()).get()
					.addHeader("X-RapidAPI-Host", apiUrl)
					.addHeader("X-RapidAPI-Key", apiKey).build();

			OkHttpClient client = new OkHttpClient();
			Response response = client.newCall(request).execute();

			int status = response.code();

			if (status >= 200 && status < 300) {
				String jsonString = response.body().string();

				Gson g = new Gson();
				BuscaFilmeModel retorno = g.fromJson(jsonString, BuscaFilmeModel.class);

				return retorno;
			}

			throw new Exception("Status de retorno não esperado: " + status);
		} catch (Exception ex) {
			Console.printarComentario(ex.getMessage(), true);
			return null;
		}

	}

	public DetalhesFilmeModel retornarDetalhesFilme(String id) throws IOException {

		try {
			StringBuilder sbUrlComParametros = new StringBuilder("");
			sbUrlComParametros.append(apiProtocol);
			sbUrlComParametros.append(apiUrl);
			sbUrlComParametros.append("/title/get-details");
			sbUrlComParametros.append(String.format("?tconst=%s", id));

			Request request = new Request.Builder()
					.url(sbUrlComParametros.toString()).get()
					.addHeader("X-RapidAPI-Host", this.apiUrl)
					.addHeader("X-RapidAPI-Key", this.apiKey).build();

			OkHttpClient client = new OkHttpClient();
			Response response = client.newCall(request).execute();

			int status = response.code();

			if (status >= 200 && status < 300) {
				String jsonString = response.body().string();

				Gson g = new Gson();
				DetalhesFilmeModel retorno = g.fromJson(jsonString, DetalhesFilmeModel.class);
				
				return retorno;
			}

			throw new Exception("Status de retorno não esperado: " + status);
		} catch (Exception ex) {
			Console.printarComentario(ex.getMessage(), true);
			return null;
		}

	}

	private SinopseFilmeModel retornarSinopseFilme(String id) throws IOException {

		try {
			StringBuilder sbUrlComParametros = new StringBuilder("");
			sbUrlComParametros.append(apiProtocol);
			sbUrlComParametros.append(apiUrl);
			sbUrlComParametros.append("/title/get-synopses");
			sbUrlComParametros.append(String.format("?tconst=%s", id));

			Request request = new Request.Builder()
					.url(sbUrlComParametros.toString()).get()
					.addHeader("X-RapidAPI-Host", this.apiUrl)
					.addHeader("X-RapidAPI-Key", this.apiKey).build();

			OkHttpClient client = new OkHttpClient();
			Response response = client.newCall(request).execute();

			int status = response.code();

			if (status >= 200 && status < 300) {
				String jsonString = response.body().string();

				Gson g = new Gson();
				Type collectionType = new TypeToken<List<SinopseFilmeModel>>(){}.getType();
				List<SinopseFilmeModel> enums = g.fromJson(jsonString, collectionType);

				if(enums.isEmpty())
				{
					return null;
				}
				
				return enums.get(0);
			}

			throw new Exception("Status de retorno não esperado: " + status);
		} catch (Exception ex) {
			Console.printarComentario(ex.getMessage(), true);
			return null;
		}

	}

	public List<ComandoBot> retornarListaComandos(String comandoPai) {

		List<ComandoBot> listaComandos = new ArrayList<ComandoBot>();

		listaComandos.add(new ComandoBot("/buscarFilme", "Buscar filme", fBuscarFilme(), comandoPai));

		return listaComandos;
	}

	private Function<String, String> fBuscarFilme() {
		return parametro -> {

			if(parametro == null) {
				return "Para buscar por um filme, utilize o seguinte formato de busca:\n/buscarfilme \"Nome do Filme\"";
			}

			try {
				BuscaFilmeModel buscaFilme = buscarFilme(parametro);

				List<FilmeModel> listaFilmes = buscaFilme.getListaFilmes();

				if (listaFilmes.isEmpty()) {
					return String.format("Nenhum resultado para '%s'", parametro);
				}
				
				for (int i = 0; i < listaFilmes.size(); i++) {
					String comando = String.format("/%s", i);
					FilmeModel filme = listaFilmes.get(i);
					ComandoBot comandoBot = new ComandoBot(comando, filme.getNome(), fDetalhesFilme(comando, filme), "/buscarFilme");
					GerenciadorComandosBot.adicionarComando(comandoBot);
				}
				
				GerenciadorComandosBot.adicionarComandoNaPilha("/buscarFilme");

				return String.format("Resultados para '%s':", parametro);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "Erro ao buscar filme";
			}
		};
	}

	private Function<String, String> fDetalhesFilme(String comando, FilmeModel filme) {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha(comando);

			try {
				DetalhesFilmeModel detalhesFilme = retornarDetalhesFilme(filme.getId());
				
				detalhesFilme.setSinopse(retornarSinopseFilme(filme.getId()));

				StringBuilder sbInfoFilme = new StringBuilder("");

				sbInfoFilme.append(String.format("Top #%o", filme.getRank()));
				sbInfoFilme.append(String.format("\n%s (%o)", filme.getNome(), detalhesFilme.getYear()));
				sbInfoFilme.append(String.format("\nCategoria: %s", filme.getCategoria()));
				sbInfoFilme.append(String.format("\nAtores Principais: %s", filme.getAtoresPrincipais()));
				
				SinopseFilmeModel objSinopse = detalhesFilme.getSinopse();
				
				if(objSinopse != null) {					
					sbInfoFilme.append(String.format("\n\nSinopse (%s): \n%s", objSinopse.getLanguage(), objSinopse.getText()));
				}
				
				return sbInfoFilme.toString();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "Erro ao buscar detalhes do filme";
			}
		};
	}
}
