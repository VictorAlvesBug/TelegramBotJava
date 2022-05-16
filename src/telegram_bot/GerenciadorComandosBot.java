package telegram_bot;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Stream;

public class GerenciadorComandosBot {

	public static ComandoBot[] arrayTodosComandos;
	private static Stack<String> pilhaComandosPai;

	private static MoviesApi moviesApi;

	public static void inicializar(String apiKeyMovies) {
		// Collection que armazenar� os comandos dispon�veis
		// mapComandos = new HashMap<String, ComandoBot>();
		arrayTodosComandos = new ComandoBot[] {
				new ComandoBot("/help", "Exibir lista de comandos dispon�veis", funcaoHelp()),
				new ComandoBot("/nomedev", "Exibir o nome do desenvolvedor do bot", funcaoNomeDev(), "/inicio"),
				new ComandoBot("/dataatual", "Informa a �ltima data de atualiza��o do bot", funcaoDataAtual(),
						"/inicio"),
				new ComandoBot("/alfanumericos", "Grupo alfanumericos", funcaoAlfanumericos(), "/inicio"),
				new ComandoBot("/letras", "Grupo letras", funcaoLetras(), "/alfanumericos"),
				new ComandoBot("/letrasmaiusculas", "Grupo letrasmaiusculas", funcaoLetrasMaiusculas(), "/letras"),
				new ComandoBot("/letrasminusculas", "Grupo letrasminusculas", funcaoLetrasMinusculas(), "/letras"),
				new ComandoBot("/numeros", "Grupo numeros", funcaoNumeros(), "/alfanumericos"),
				new ComandoBot("/numerospares", "Grupo numerospares", funcaoNumerosPares(), "/numeros"),
				new ComandoBot("/numerosimpares", "Grupo numerosimpares", funcaoNumerosImpares(), "/numeros"),
				new ComandoBot("/buscarfilme", "Buscar filme", funcaoBuscarFilme(), "/inicio"),
				new ComandoBot("/voltar", "Retornar para o comando anterior", funcaoVoltar()) };

		// Instancia a pilha de comandos pai
		pilhaComandosPai = new Stack<String>();
		pilhaComandosPai.add("/inicio");

		moviesApi = new MoviesApi(apiKeyMovies);
	}

	private static String getComandoPaiAtual() {
		if (pilhaComandosPai.empty()) {
			return "/inicio";
		}

		return pilhaComandosPai.get(pilhaComandosPai.size() - 1);
	}

	public static void adicionarComandoNaPilha(String comando) {
		pilhaComandosPai.add(comando);
	}

	public static void voltarUmComandoNaPilha() {
		pilhaComandosPai.pop();
	}

	public static void voltarAoComandoInicialDaPilha() {
		pilhaComandosPai = new Stack<String>();
		pilhaComandosPai.add("/inicio");
	}

	public static void printarStrPilha() {
		Console.printarComentario(pilhaComandosPai.toString());
	}

	public static boolean verificarSeComandoAlterouPilha(String comando) {
		return comando.equals("/voltar") || getComandoPaiAtual().equals(comando);
	}

	public static String tentarExecutarFuncao(String comando) {
		// Caso n�o exista
		if (!verificarSeComandoExiste(comando)) {
			// Informa no console que o comando n�o existe
			Console.printarComentario(String.format("Comando %s n�o encontrado", comando));
			return null;
		}

		// Busca comando-filho do comando-pai atual
		List<ComandoBot> listaComandosFilhos = retornarListaComandosFilhos(comando);

		// Caso o comando n�o seja filho do comando pai atual
		if (listaComandosFilhos.isEmpty()) {
			// Informa no console que o comando n�o est� dispon�vel no momento
			return String.format("Comando %s n�o est� dispon�vel neste ponto da conversa", comando);
		}

		// executa a fun��o atrelada ao comando filho
		// TODO: Permitir a passagem de informa��es por argumento
		return listaComandosFilhos.get(0).executarFuncao(null);
	}

	public static boolean verificarSeComandoExiste(String comandoBuscado) {
		Stream<ComandoBot> streamComandosEncontrados = Arrays.stream(arrayTodosComandos).filter(comandoBot -> {
			return comandoBot.getComando().equals(comandoBuscado);
		});
		return streamComandosEncontrados.toList().size() > 0;
	}

	public static List<ComandoBot> retornarListaComandosFilhos(String comandoBuscado) {
		Stream<ComandoBot> streamComandosEncontrados = Arrays.stream(arrayTodosComandos).filter(comandoBot -> {
			return (comandoBuscado == null || comandoBot.getComando().equals(comandoBuscado))
					&& (comandoBot.getComandoPai() == null || comandoBot.getComandoPai().equals(getComandoPaiAtual()));
		});

		return streamComandosEncontrados.toList();
	}

	public static String retornarStrListaComandos() {
		// Retorna array de comandos dispon�veis
		List<ComandoBot> listaComandosFilhos = retornarListaComandosFilhos(null);

		// Caso n�o encontre nenhum comando
		if (listaComandosFilhos.size() == 0) {
			return "Nenhum comando encontrado";
		}

		// Inicializa um buffer de strings, para concatenar os comandos dispon�veis
		StringBuilder stringBuilderComandos = new StringBuilder("Comandos dispon�veis:");

		// Adiciona cada um dos comandos no buffer de strings
		for (ComandoBot comandoBot : listaComandosFilhos) {
			stringBuilderComandos.append("\n");
			stringBuilderComandos.append(comandoBot.getInfo());
		}

		// Retorna string resultante do buffer
		return stringBuilderComandos.toString();
	}

	/// Fun��es executadas pelos comandos

	private static Function<String, String> funcaoHelp() {
		// Retorna a lista de comandos dispon�veis
		return parametro -> retornarStrListaComandos();
	}

	private static Function<String, String> funcaoNomeDev() {
		// Retorna o nome do criador do chatbot
		return parametro -> "Chatbot criado por Victor Alves Bugueno";
	}

	private static Function<String, String> funcaoDataAtual() {
		// Retorna o hor�rio atual
		return parametro -> "Agora s�o: " + Console.retornarDataHoraAtual("HH:mm (dd/MM/yyyy)");
	}

	private static Function<String, String> funcaoAlfanumericos() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/alfanumericos");
			return "Voc� selecionou o grupo 'alfanumericos'";
		};
	}

	private static Function<String, String> funcaoLetras() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/letras");
			return "Voc� selecionou o grupo 'letras'";
		};
	}

	private static Function<String, String> funcaoLetrasMaiusculas() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/letrasmaiusculas");
			return "Voc� selecionou o grupo 'letrasmaiusculas'";
		};
	}

	private static Function<String, String> funcaoLetrasMinusculas() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/letrasminusculas");
			return "Voc� selecionou o grupo 'letrasminusculas'";
		};
	}

	private static Function<String, String> funcaoNumeros() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/numeros");
			return "Voc� selecionou o grupo 'numeros'";
		};
	}

	private static Function<String, String> funcaoNumerosPares() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/numerospares");
			return "Voc� selecionou o grupo 'numerospares'";
		};
	}

	private static Function<String, String> funcaoNumerosImpares() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/numerosimpares");
			return "Voc� selecionou o grupo 'numerosimpares'";
		};
	}

	private static Function<String, String> funcaoVoltar() {
		return parametro -> {
			String comandoPaiAnterior = GerenciadorComandosBot.getComandoPaiAtual();
			GerenciadorComandosBot.voltarUmComandoNaPilha();
			String comandoPaiAtual = GerenciadorComandosBot.getComandoPaiAtual();

			if (comandoPaiAnterior == comandoPaiAtual) {
				return "Voc� j� se encontra no comando inicial";
			}

			return "Voc� retornou para o comando " + comandoPaiAtual;
		};
	}

	private static Function<String, String> funcaoBuscarFilme() {
		return parametro -> {

			List<String> listaFilmes;

			try {
				// listaFilmes = moviesApi.buscarFilme(parametro);
				listaFilmes = moviesApi.buscarFilme("game");

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
