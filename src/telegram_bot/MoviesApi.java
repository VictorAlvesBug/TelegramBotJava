package telegram_bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import telegram_bot.Models.BuscaFilmeModel;
import telegram_bot.Models.DetalhesFilmeModel;
import telegram_bot.Models.FilmeModel;

public class MoviesApi {
	private String apiProtocol;
	private String apiUrl;
	private String apiKey;

	public MoviesApi(String apiKey) throws Exception {

		// Caso não seja informada a apiKey, lança exceção
		if (apiKey == null || apiKey.isEmpty()) {
			throw new Exception("A apiKey informada é inválida");
		}

		this.apiProtocol = "https://";
		this.apiUrl = "online-movie-database.p.rapidapi.com";
		this.apiKey = apiKey;
	}

	public BuscaFilmeModel buscarFilme(String termo) throws IOException {

		try {
			// Constroi a url da requisição
			StringBuilder sbUrlComParametros = new StringBuilder("");
			sbUrlComParametros.append(apiProtocol);
			sbUrlComParametros.append(apiUrl);
			sbUrlComParametros.append("/auto-complete");
			sbUrlComParametros.append(String.format("?q=%s", termo));

			// Efetua a requisição
			Request request = new Request.Builder().url(sbUrlComParametros.toString()).get()
					.addHeader("X-RapidAPI-Host", apiUrl).addHeader("X-RapidAPI-Key", apiKey).build();

			// Armazena o retorno da API
			OkHttpClient client = new OkHttpClient();
			Response response = client.newCall(request).execute();

			// Armazena o statusCode da requisição
			int status = response.code();

			// statusCode de 200 até 299 é considerado sucesso
			if (status >= 200 && status < 300) {
				// Recupera dados retornados da API e popula objeto da classe BuscaFilmeModel
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
			// Constroi a url da requisição
			StringBuilder sbUrlComParametros = new StringBuilder("");
			sbUrlComParametros.append(apiProtocol);
			sbUrlComParametros.append(apiUrl);
			sbUrlComParametros.append("/title/get-details");
			sbUrlComParametros.append(String.format("?tconst=%s", id));

			// Efetua a requisição
			Request request = new Request.Builder().url(sbUrlComParametros.toString()).get()
					.addHeader("X-RapidAPI-Host", this.apiUrl).addHeader("X-RapidAPI-Key", this.apiKey).build();

			// Armazena o retorno da API
			OkHttpClient client = new OkHttpClient();
			Response response = client.newCall(request).execute();

			// Armazena o statusCode da requisição
			int status = response.code();

			// statusCode de 200 até 299 é considerado sucesso
			if (status >= 200 && status < 300) {
				// Recupera dados retornados da API e popula objeto da classe DetalhesFilmeModel
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

	// Acrescenta os comandos referentes a API de filmes na lista de comandos disponíveis no contexto raiz
	public List<ComandoBot> retornarListaComandos(String comandoPai) {

		// Instancia a lista de comandos
		List<ComandoBot> listaComandos = new ArrayList<ComandoBot>();

		// Função responsável por retornar as informações detalhadas de um filme
		IFuncao fDetalhesFilme = objFilme -> {
			FilmeModel filme = FilmeModel.class.cast(objFilme);

			GerenciadorComandosBot.getInstancia().adicionarComandoNaPilha(filme.getComando());

			StringBuilder sbInfoFilme = new StringBuilder("");

			sbInfoFilme.append(String.format("Top #%o", filme.getRank()));
			sbInfoFilme.append(String.format("\n%s (%o)", filme.getNome(), filme.getAnoLancamento()));
			sbInfoFilme.append(String.format("\nCategoria: %s", filme.getCategoria()));
			sbInfoFilme.append(String.format("\nAtores Principais: %s", filme.getAtoresPrincipais()));

			return sbInfoFilme.toString();
		};

		// Funça que permite buscar um filme utilizando um parâmetro
		listaComandos.add(new ComandoBot("/buscarFilme", "Buscar filme", termo -> {
			String strTermo = (String) termo;
			if (strTermo == null || strTermo.isEmpty()) {
				return "Para buscar por um filme, utilize o seguinte formato de busca:\n/buscarfilme \"Nome do Filme\"";
			}

			try {
				List<FilmeModel> listaFilmes = buscarFilme(strTermo).getListaFilmes();

				if (listaFilmes.isEmpty()) {
					return String.format("Nenhum resultado para \"%s\"", strTermo);
				}

				for (int i = 0; i < listaFilmes.size(); i++) {
					String comando = String.format("/%s", i);
					FilmeModel filme = listaFilmes.get(i);

					ComandoBot comandoBot = new ComandoBot(comando, filme.getNome(), fDetalhesFilme, "/buscarFilme");
					comandoBot.setObjInformacoesAdicionais(filme);
					GerenciadorComandosBot.getInstancia().adicionarComando(comandoBot);
				}

				GerenciadorComandosBot.getInstancia().adicionarComandoNaPilha("/buscarFilme");

				return String.format("Resultados para \"%s\":", strTermo);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return "Erro ao buscar filme";
			}
		}, comandoPai));

		return listaComandos;
	}
}
