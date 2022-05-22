package telegram_bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

// Classe no padr�o Singleton:
// Garante que s� existir� uma inst�ncia da classe e que todas as requisi��es para esta classe ser�o 
// reunidas nesta inst�ncia.
public class GerenciadorComandosBot {

	private static GerenciadorComandosBot instancia;

	// Lista de comandos aceitos
	private List<ComandoBot> listaTodosComandos;

	// Pilha de comandos executados em arvore.
	// Similar a uma entrada em diret�rios que possuem uma lista de diret�rios
	// internos.
	// O contexto muda, ent�o nem todos os comandos dispon�veis em um n�vel est�o
	// dispon�veis em outro.
	private Stack<String> pilhaComandosPai;

	// Armazena a �ltima mensagem de help (lista de comandos) enviada para evitar
	// ficar buscando entre
	// os comandos dispon�veis no momento
	private String helpAnterior = null;

	// Objeto que efetua requisi��es para a api de filmes e define os comandos
	// dispon�veis em rela��o a
	// este contexto
	private MoviesApi moviesApi;

	// Construtor da classe � privado, de forma que apenas a pr�pria classe � capaz
	// de instanci�-la.
	private GerenciadorComandosBot(String apiKeyMovies) throws Exception {

		// Inicializa a "fachada" da API de filmes
		moviesApi = new MoviesApi(apiKeyMovies);

		// O comando pai inicial � sempre "/inicio"
		String comandoPai = "/inicio";

		// Instancia a lista de comandos
		listaTodosComandos = new ArrayList<ComandoBot>();

		// Primeiro comando da lista, totalmente independente (pode ser executado a
		// qualquer momento)
		instancia.adicionarComando(new ComandoBot("/help", "Exibir lista de comandos dispon�veis", a -> instancia.getHelp()));

		// Adiciona comandos b�sicos
		instancia.adicionarComando((new ComandosBasicos()).retornarListaComandos(comandoPai));

		// Adiciona comandos espec�ficos da API de filmes
		instancia.adicionarComando(moviesApi.retornarListaComandos(comandoPai));

		// �ltimos comandos da lista, totalmente independente (pode ser executado a
		// qualquer momento)
		instancia.adicionarComando(new ComandoBot("/voltar", "Retornar para o comando anterior", a -> {
			// Recupera o comando pai (contexto)
			String comandoPaiAnterior = instancia.getComandoPaiAtual();

			// Caso o comando pai seja o inicial, exibe mensagem dizendo que j� est� no
			// comando inicial
			if (comandoPaiAnterior.equals("/inicio")) {
				return "Voc� j� se encontra no comando inicial";
			}

			// Caso ainda n�o esteja no comendo inicial, volta um comando na pilha e exibe
			// mensagem informando para qual comando foi retornado.
			instancia.voltarUmComandoNaPilha();
			String comandoPaiAtual = instancia.getComandoPaiAtual();
			return "Voc� retornou para o comando " + comandoPaiAtual;
		}));

		// Vai direto para o comando inicio
		adicionarComando(new ComandoBot("/inicio", "Retornar para o comando in�cio", a -> {
			instancia.voltarAoComandoInicialDaPilha();
			return "Voc� retornou para o comando inicial";
		}));

		// Instancia a pilha de comandos pai
		pilhaComandosPai = new Stack<String>();
		pilhaComandosPai.add(comandoPai);
	}

	// M�todo est�tico para retornar a inst�ncia atual da classe Singleton, sem
	// par�metros
	public static GerenciadorComandosBot getInstancia() {
		return getInstancia(null);
	}

	// M�todo est�tico para retornar a inst�ncia atual da classe Singleton, com um
	// par�metro
	public static GerenciadorComandosBot getInstancia(String apiKeyMovies) {
		try {
			// Caso a inst�ncia atual seja nula, efetua uma inst�ncia
			if (instancia == null) {
				instancia = new GerenciadorComandosBot(apiKeyMovies);
			}
			// Retorna a inst�ncia atual
			return instancia;
		} catch (Exception ex) {
			return null;
		}
	}

	public String getHelp() {
		// Retorna o help (lista de comandos) anterior caso n�o esteja nula
		if (helpAnterior == null) {
			return instancia.retornarStrListaComandos();
		}

		return helpAnterior;
	}

	// Retorna a posi��o mais elevada da pilha
	public String getComandoPaiAtual() {
		if (pilhaComandosPai.empty()) {
			return "/inicio";
		}

		return pilhaComandosPai.get(pilhaComandosPai.size() - 1);
	}

	// Adiciona um comando na pilha, entrando em um novo contexto
	public void adicionarComandoNaPilha(String comando) {
		pilhaComandosPai.add(comando);
	}

	// Retorna um comando na pilha, voltando para o contexto acima
	public void voltarUmComandoNaPilha() {
		pilhaComandosPai.pop();
	}

	// Retorna um comando na pilha, voltando para o contexto inicial
	public void voltarAoComandoInicialDaPilha() {
		pilhaComandosPai = new Stack<String>();
		pilhaComandosPai.add("/inicio");
	}

	// Exibe a pilha em forma de breadcrumb
	public String retornarStrPilha() {
		return pilhaComandosPai.stream().reduce("", (acc, item) -> {
			return String.format("%s > %s", acc, item);
		});
	}

	// Caso o comando seja o /voltar, ou caso o comando seja o comando pai,
	// significa que este comando alterou a pilha
	public boolean verificarSeComandoAlterouPilha(String comando) {
		return comando.equals("/voltar") || instancia.getComandoPaiAtual().equals(comando);
	}

	// Adiciona v�rios comandos na lista
	public void adicionarComando(List<ComandoBot> listaComandoBot) {
		for (int i = 0; i < listaComandoBot.size(); i++) {
			instancia.adicionarComando(listaComandoBot.get(i));
		}
	}

	// Adiciona um comando na lista (caso j� tenha algum comando com o mesmo nome,
	// substitui)
	public void adicionarComando(ComandoBot comandoBot) {
		// Antes de adicionar, remove qualquer comando que tenha este nome, para evitar
		// duplicidades
		listaTodosComandos.removeIf(c -> c.getComando().equals(comandoBot.getComando()));
		listaTodosComandos.add(comandoBot);
	}

	// Tenta rodar o trecho de c�digo vinculado ao comando, aplicando os par�metros
	// passados
	public String tentarExecutarFuncao(String comando, String parametros) {
		// Caso n�o exista
		if (!instancia.verificarSeComandoExiste(comando)) {
			// Informa no console que o comando n�o existe
			Console.printarComentario(String.format("Comando %s n�o encontrado", comando));
			return null;
		}

		// Busca comando-filho do comando-pai atual
		List<ComandoBot> listaComandosFilhos = instancia.retornarListaComandosFilhos(comando);

		// Caso o comando n�o seja filho do comando pai atual
		if (listaComandosFilhos.isEmpty()) {
			// Informa no console que o comando n�o est� dispon�vel no momento
			return String.format("Comando %s n�o est� dispon�vel neste ponto da conversa", comando);
		}

		// executa a fun��o atrelada ao comando filho
		// TODO: Permitir a passagem de informa��es por argumento
		return listaComandosFilhos.get(0).executarFuncao(parametros);
	}

	// Verifica se comando buscado na lista de comandos dispon�veis
	public boolean verificarSeComandoExiste(String comandoBuscado) {
		Stream<ComandoBot> streamComandosEncontrados = listaTodosComandos.stream().filter(comandoBot -> {
			return comandoBot.getComando().equals(comandoBuscado);
		});
		return streamComandosEncontrados.toList().size() > 0;
	}

	// Busca comando na lista de comandos filhos ou na lista de comandos
	// independentes, e retorna o comando compat�vel
	public List<ComandoBot> retornarListaComandosFilhos(String comandoBuscado) {
		Stream<ComandoBot> streamComandosEncontrados = listaTodosComandos.stream().filter(comandoBot -> {
			return (comandoBuscado == null || comandoBot.getComando().equals(comandoBuscado))
					&& (comandoBot.getComandoPai() == null || comandoBot.getComandoPai().equals(instancia.getComandoPaiAtual()));
		});

		return streamComandosEncontrados.toList();
	}

	public String retornarStrListaComandos() {
		return instancia.retornarStrListaComandos("");
	}

	public String retornarStrListaComandos(String conteudoResposta) {
		// Retorna array de comandos dispon�veis
		List<ComandoBot> listaComandosFilhos = instancia.retornarListaComandosFilhos(null);

		// Caso n�o encontre nenhum comando
		if (listaComandosFilhos.size() == 0) {
			return "Nenhum comando encontrado";
		}

		String breadcrumb = String.format("Voc� est� em: \n%s", instancia.retornarStrPilha());

		// Inicializa um buffer de strings, para concatenar os comandos dispon�veis
		StringBuilder stringBuilderComandos = new StringBuilder(breadcrumb);
		stringBuilderComandos.append("\n\nComandos dispon�veis:");

		// Adiciona cada um dos comandos no buffer de strings
		for (ComandoBot comandoBot : listaComandosFilhos) {
			stringBuilderComandos.append("\n" + comandoBot.getInfo());

			if (comandoBot.getComando().equals("/inicio") && !conteudoResposta.isEmpty()) {
				stringBuilderComandos.append("\n\n" + conteudoResposta);
			}
		}

		// Atualiza help anterior, para que na pr�xima requisi��o da lista n�o seja
		// necess�rio rodar toda a busca novamente
		helpAnterior = stringBuilderComandos.toString();

		// Retorna string resultante do buffer
		return helpAnterior;
	}
}
