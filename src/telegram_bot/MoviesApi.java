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

		// Caso n?o seja informada a apiKey, lan?a exce??o
		if (apiKey == null || apiKey.isEmpty()) {
			throw new Exception("A apiKey informada ? inv?lida");
		}

		// Armazena as informa??es para fazer as futuras requisi??es ? API de filmes
		this.apiProtocol = "https://";
		this.apiUrl = "online-movie-database.p.rapidapi.com";
		this.apiKey = apiKey;
	}

	public BuscaFilmeModel buscarFilme(String termo) throws IOException {
		try {
			// Constr?i a url da requisi??o
			StringBuilder sbUrlComParametros = new StringBuilder("");
			sbUrlComParametros.append(apiProtocol);
			sbUrlComParametros.append(apiUrl);
			sbUrlComParametros.append("/title/find");
			sbUrlComParametros.append(String.format("?q=%s", termo));

			// Efetua a requisi??o
			Request request = new Request.Builder().url(sbUrlComParametros.toString()).get()
					.addHeader("X-RapidAPI-Host", apiUrl).addHeader("X-RapidAPI-Key", apiKey).build();

			// Armazena o retorno da API
			OkHttpClient client = new OkHttpClient();
			Response response = client.newCall(request).execute();

			// Armazena o statusCode da requisi??o
			int status = response.code();

			// statusCode de 200 at? 299 ? considerado sucesso
			if (status >= 200 && status < 300) {
				// Recupera dados retornados da API e popula objeto da classe BuscaFilmeModel
				String jsonString = response.body().string();
				BuscaFilmeModel retorno = (new Gson()).fromJson(jsonString, BuscaFilmeModel.class);

				return retorno;
			}

			throw new Exception("Status de retorno n?o esperado: " + status);
		} catch (Exception ex) {
			Console.printarComentario(ex.getMessage(), true);
			return null;
		}

	}

	public DetalhesFilmeModel retornarDetalhesFilme(String id) throws IOException {

		try {
			// Constroi a url da requisi??o
			StringBuilder sbUrlComParametros = new StringBuilder("");
			sbUrlComParametros.append(apiProtocol);
			sbUrlComParametros.append(apiUrl);
			sbUrlComParametros.append("/title/get-details");
			sbUrlComParametros.append(String.format("?tconst=%s", id));

			// Efetua a requisi??o
			Request request = new Request.Builder().url(sbUrlComParametros.toString()).get()
					.addHeader("X-RapidAPI-Host", this.apiUrl).addHeader("X-RapidAPI-Key", this.apiKey).build();

			// Armazena o retorno da API
			OkHttpClient client = new OkHttpClient();
			Response response = client.newCall(request).execute();

			// Armazena o statusCode da requisi??o
			int status = response.code();

			// statusCode de 200 at? 299 ? considerado sucesso
			if (status >= 200 && status < 300) {
				// Recupera dados retornados da API e popula objeto da classe DetalhesFilmeModel
				String jsonString = response.body().string();
				DetalhesFilmeModel retorno = (new Gson()).fromJson(jsonString, DetalhesFilmeModel.class);

				return retorno;
			}

			throw new Exception("Status de retorno n?o esperado: " + status);
		} catch (Exception ex) {
			Console.printarComentario(ex.getMessage(), true);
			return null;
		}

	}

	// Acrescenta os comandos referentes a API de filmes na lista de comandos
	// dispon?veis no contexto raiz
	public List<ComandoBot> retornarListaComandos(String comandoPai, GerenciadorComandosBot gerenciadorComandos) {
		// Fun??o respons?vel por retornar as informa??es detalhadas de um filme
		IFuncao fDetalhesFilme = objFilme -> {
			FilmeModel filme = FilmeModel.class.cast(objFilme);

			StringBuilder sbInfoFilme = new StringBuilder();

			sbInfoFilme.append(String.format("%s (%o)", filme.getNome(), filme.getAnoLancamento()));
			sbInfoFilme.append(String.format("\nCategoria: %s", filme.getCategoria()));
			sbInfoFilme.append(String.format("\nAtores Principais: %s", filme.getAtoresPrincipais()));
			sbInfoFilme.append(String.format("\nImagem: %s", filme.getUrlImagem()));

			return sbInfoFilme.toString();
		};

		// Instancia a lista de comandos
		List<ComandoBot> listaComandos = new ArrayList<ComandoBot>();

		// Fun??o que permite buscar um filme utilizando um par?metro
		listaComandos.add(new ComandoBot("/buscarFilme", "Buscar filme", termo -> {
			String strTermo = (String) termo;
			// Caso n?o tenha sido informado o nome do filme por par?metro, informa ao
			// usu?rio como buscar por um filme
			if (strTermo == null || strTermo.isEmpty()) {
				return "Para buscar por um filme, utilize o seguinte formato de busca:\n/buscarFilme \"Nome do Filme\"";
			}

			try {
				// Retorna lista de filmes compat?veis com o termo buscado
				List<FilmeModel> listaFilmes = buscarFilme(strTermo).getListaFilmes();

				// Caso a lista esteja vazia, informa que n?o foi encontrado nenhum filme com
				// este nome
				if (listaFilmes.isEmpty()) {
					return String.format("Nenhum resultado para \"%s\"", strTermo);
				}

				// Caso contr?rio, itera cada um dos filmes e adiciona um comando respons?vel
				// por exibir mais informa??es sobre este filme
				for (int i = 0; i < listaFilmes.size(); i++) {
					String comando = String.format("/%s", i);
					FilmeModel filme = listaFilmes.get(i);

					ComandoBot comandoBot = new ComandoBot(comando, filme.getNome(), fDetalhesFilme, "/buscarFilme");
					comandoBot.setObjInformacoesAdicionais(filme);
					gerenciadorComandos.adicionarComando(comandoBot);
				}

				// Entra no contexto /buscarFilme
				gerenciadorComandos.adicionarComandoNaPilha("/buscarFilme");

				return String.format("Resultados para \"%s\":", strTermo);
			} catch (IOException e) {
				return "Erro ao buscar filme";
			}
		}, comandoPai));

		return listaComandos;
	}
}
