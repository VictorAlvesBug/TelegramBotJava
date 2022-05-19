package telegram_bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Stream;

public class GerenciadorComandosBot {

	private static List<ComandoBot> listaTodosComandos;
	private static Stack<String> pilhaComandosPai;
	private static String helpAnterior = null;

	private static MoviesApi moviesApi;

	public static void inicializar(String apiKeyMovies) {
		moviesApi = new MoviesApi(apiKeyMovies);
		
		String comandoPai = "/inicio";
		
		listaTodosComandos = new ArrayList<ComandoBot>();

		// Primeiro comando da lista, totalmente independente (pode ser executado a qualquer momento)
		adicionarComando(new ComandoBot("/help", "Exibir lista de comandos dispon�veis", fHelp()));
		
		// Adiciona comandos b�sicos
		adicionarComando((new ComandosBasicos()).retornarListaComandos(comandoPai));
		
		// Adiciona comandos espec�ficos de cada API
		adicionarComando(moviesApi.retornarListaComandos(comandoPai));
		
		// �ltimos comandos da lista, totalmente independente (pode ser executado a qualquer momento)
		adicionarComando(new ComandoBot("/voltar", "Retornar para o comando anterior", fVoltar()));
		adicionarComando(new ComandoBot("/inicio", "Retornar para o comando in�cio", fInicio()));
		
		// Instancia a pilha de comandos pai
		pilhaComandosPai = new Stack<String>();
		pilhaComandosPai.add(comandoPai);
	}
	
	public static String getHelp() {
		if(helpAnterior == null) {
			return retornarStrListaComandos();
		}
		
		return helpAnterior;
	}

	static String getComandoPaiAtual() {
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

	public static String retornarStrPilha() {
		StringBuilder sbPilha = new StringBuilder("");
		
		return pilhaComandosPai.stream().reduce("", (acc, item) -> {
			return String.format("%s > %s", acc, item);
		});
	}

	public static boolean verificarSeComandoAlterouPilha(String comando) {
		return comando.equals("/voltar") || getComandoPaiAtual().equals(comando);
	}

	public static void adicionarComando(List<ComandoBot> listaComandoBot) {
		for(int i=0; i<listaComandoBot.size();i++) {
			adicionarComando(listaComandoBot.get(i));
		}
	}

	public static void adicionarComando(ComandoBot comandoBot) {
		// Antes de adicionar, remove qualquer comando que tenha este nome, para evitar duplicidades
		listaTodosComandos.removeIf(c -> c.getComando().equals(comandoBot.getComando()));
		listaTodosComandos.add(comandoBot);
	}
	
	public static String tentarExecutarFuncao(String comando, String parametros) {
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
		return listaComandosFilhos.get(0).executarFuncao(parametros);
	}

	public static boolean verificarSeComandoExiste(String comandoBuscado) {
        ComandoBot[] arrayTodosComandos = new ComandoBot[listaTodosComandos.size()];
        arrayTodosComandos = listaTodosComandos.toArray(arrayTodosComandos);
		
		Stream<ComandoBot> streamComandosEncontrados = Arrays.stream(arrayTodosComandos).filter(comandoBot -> {
			return comandoBot.getComando().equals(comandoBuscado);
		});
		return streamComandosEncontrados.toList().size() > 0;
	}

	public static List<ComandoBot> retornarListaComandosFilhos(String comandoBuscado) {
        ComandoBot[] arrayTodosComandos = new ComandoBot[listaTodosComandos.size()];
        arrayTodosComandos = listaTodosComandos.toArray(arrayTodosComandos);
        
		Stream<ComandoBot> streamComandosEncontrados = Arrays.stream(arrayTodosComandos).filter(comandoBot -> {
			return (comandoBuscado == null || comandoBot.getComando().equals(comandoBuscado))
					&& (comandoBot.getComandoPai() == null || comandoBot.getComandoPai().equals(getComandoPaiAtual()));
		});

		return streamComandosEncontrados.toList();
	}

	public static String retornarStrListaComandos() {
		return retornarStrListaComandos("");
	}

	public static String retornarStrListaComandos(String conteudoResposta) {
		// Retorna array de comandos dispon�veis
		List<ComandoBot> listaComandosFilhos = retornarListaComandosFilhos(null);

		// Caso n�o encontre nenhum comando
		if (listaComandosFilhos.size() == 0) {
			return "Nenhum comando encontrado";
		}

		String breadcrumb = String.format("Voc� est� em: \n%s", retornarStrPilha());
		
		// Inicializa um buffer de strings, para concatenar os comandos dispon�veis
		StringBuilder stringBuilderComandos = new StringBuilder(breadcrumb);
		stringBuilderComandos.append("\n\nComandos dispon�veis:");

		// Adiciona cada um dos comandos no buffer de strings
		for (ComandoBot comandoBot : listaComandosFilhos) {
			stringBuilderComandos.append("\n");
			stringBuilderComandos.append(comandoBot.getInfo());
			
			if(comandoBot.getComando().equals("/inicio") && !conteudoResposta.isEmpty()) {
				stringBuilderComandos.append("\n");
				stringBuilderComandos.append("\n");
				stringBuilderComandos.append(conteudoResposta);
			}
		}
		
		helpAnterior = stringBuilderComandos.toString();

		// Retorna string resultante do buffer
		return helpAnterior;
	}
	
	private static Function<String, String> fHelp() {
		// Retorna a lista de comandos dispon�veis
		return parametro -> {
			return getHelp();
		};
	}

	private static Function<String, String> fVoltar() {
		return parametro -> {
			String comandoPaiAnterior = GerenciadorComandosBot.getComandoPaiAtual();
			GerenciadorComandosBot.voltarUmComandoNaPilha();
			String comandoPaiAtual = GerenciadorComandosBot.getComandoPaiAtual();

			if (comandoPaiAnterior == comandoPaiAtual) {
				GerenciadorComandosBot.adicionarComandoNaPilha("/inicio");
				return "Voc� j� se encontra no comando inicial";
			}

			return "Voc� retornou para o comando " + comandoPaiAtual;
		};
	}
	
	private static Function<String, String> fInicio() {
		return parametro -> {
			GerenciadorComandosBot.voltarAoComandoInicialDaPilha();
			return "Voc� retornou para o comando inicial";
		};
	}
}
