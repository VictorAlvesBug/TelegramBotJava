package telegram_bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Stream;

public class GerenciadorComandosBot {
	// public static Map<String, ComandoBot> mapComandos;
	public static ComandoBot[] arrayTodosComandos;
	private static Stack<String> pilhaComandosPai;

	public static void inicializar() {
		// Collection que armazenar� os comandos dispon�veis
		// mapComandos = new HashMap<String, ComandoBot>();
		arrayTodosComandos = new ComandoBot[] {
				new ComandoBot("/help", "Exibir lista de comandos dispon�veis", funcaoHelp()),
				new ComandoBot("/nomedev", "Exibir Retorna o nome do desenvolvedor do bot", funcaoNomeDev(), "/inicio"),
				new ComandoBot("/dataatual", "Informa a �ltima data de atualiza��o do bot", funcaoDataAtual(),
						"/inicio"),
				new ComandoBot("/alfanumericos", "Grupo alfanumericos", funcaoAlfanumericos(), "/inicio"),
				new ComandoBot("/letras", "Grupo letras", funcaoLetras(), "/alfanumericos"),
				new ComandoBot("/letrasmaiusculas", "Grupo letrasmaiusculas", funcaoLetrasMaiusculas(), "/letras"),
				new ComandoBot("/letrasminusculas", "Grupo letrasminusculas", funcaoLetrasMinusculas(), "/letras"),
				new ComandoBot("/numeros", "Grupo numeros", funcaoNumeros(), "/alfanumericos"),
				new ComandoBot("/numerospares", "Grupo numerospares", funcaoNumerosPares(), "/numeros"),
				new ComandoBot("/numerosimpares", "Grupo numerosimpares", funcaoNumerosImpares(), "/numeros"),
				new ComandoBot("/inicio", "Comando Inicial", funcaoStart()) };

		// Instancia a pilha de comandos pai
		pilhaComandosPai = new Stack<String>();
		pilhaComandosPai.add("/inicio");
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

	/*
	 * public static String retornarStrListaComandos() { return
	 * retornarStrListaComandos("/inicio"); }
	 */

	public static String retornarStrListaComandos(/* String comandoPai */) {
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

	/*public static List<ComandoBot> retornarListaComandosFilhos() {
		return retornarListaComandosFilhos("/inicio");
	}

	public static List<ComandoBot> retornarListaComandosFilhos(String comandoPai) {
		// Armazena lista de comandos
		List<ComandoBot> listaComandosFilhos = new ArrayList<ComandoBot>();

		// Caso esteja buscando por comandos iniciais (sem comando pai)
		if (comandoPai == null || comandoPai.length() == 0 || comandoPai.equals("/inicio")) {
			for (ComandoBot comandoBot : arrayTodosComandos) {
				// Adiciona apenas comandos iniciais (sem comando pai)
				if (comandoBot.getComandoPai().equals("/inicio")) {
					listaComandosFilhos.add(comandoBot);
				}
			}

			return listaComandosFilhos;
		}

		// Caso esteja buscando por comandos com um pai definido
		for (ComandoBot comandoBot : arrayTodosComandos) {
			if (comandoBot.getComandoPai().equals(comandoPai)) {
				// Adiciona apenas comandos com esse comando pai em comum
				listaComandosFilhos.add(comandoBot);
			}
		}

		return listaComandosFilhos;
	}*/

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

	private static Function<String, String> funcaoStart() {
		return parametro -> {
			GerenciadorComandosBot.voltarAoComandoInicialDaPilha();
			return "Voc� retornou para o comando inicial";
		};
	}

}
