package telegram_bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

// Classe no padr�o MonoState:
// Garante que todas as inst�ncias da classe acessar�o aos mesmos atributos est�ticos.
public class GerenciadorComandosBot {
	// Lista de comandos aceitos
	private static List<ComandoBot> listaTodosComandos;

	// Pilha de comandos executados em �rvore.
	// Similar a uma entrada em diret�rios que possuem uma lista de diret�rios
	// internos.
	// O contexto muda, ent�o nem todos os comandos dispon�veis em um n�vel est�o
	// dispon�veis em outro.
	private static Stack<String> pilhaComandosPai;

	// Armazena a �ltima mensagem de help (lista de comandos) enviada para evitar
	// que sejam buscados os comandos dispon�veis sendo que s�o os mesmos
	// previamente exibidos
	private static String helpAnterior = null;

	// Objeto que efetua requisi��es para a API de filmes e define os comandos
	// dispon�veis em rela��o a este contexto
	private static MoviesApi moviesApi;

	public GerenciadorComandosBot() {
		if (moviesApi == null) {
			Console.printarComentario("Erro ao instanciar GerenciadorComandosBot", true);
			return;
		}
	}

	public GerenciadorComandosBot(String apiKeyMovies) throws Exception {

		// Caso a api de filmes j� esteja instanciada, aborta re-execu��o do construtor
		if (moviesApi != null) {
			return;
		}

		// Inicializa a "fachada" da API de filmes
		moviesApi = new MoviesApi(apiKeyMovies);

		// O comando pai inicial � sempre "/inicio"
		String comandoPai = "/inicio";

		// Instancia a lista de comandos
		listaTodosComandos = new ArrayList<ComandoBot>();

		// Primeiro comando da lista, totalmente independente (pode ser executado a
		// qualquer momento)
		this.adicionarComando(new ComandoBot("/help", "Exibir lista de comandos dispon�veis", a -> this.getHelp()));

		// Vai direto para o comando /inicio
		adicionarComando(new ComandoBot("/inicio", "Retornar para o comando inicial", a -> {
			this.voltarAoComandoInicialDaPilha();
			return "Voc� retornou para o comando inicial";
		}));

		// Adiciona comandos b�sicos
		this.adicionarComando((new ComandosBasicos()).retornarListaComandos(comandoPai, this));

		// Adiciona comandos com informa��es dos devs
		this.adicionarComando((new DevsInfo()).retornarListaComandos(comandoPai, this));

		// Adiciona comandos espec�ficos da API de filmes
		this.adicionarComando(moviesApi.retornarListaComandos(comandoPai, this));

		// Adiciona comandos referentes ao jogo da velha (tic-tac-toe)
		this.adicionarComando((new TicTacToeGame()).retornarListaComandos(comandoPai, this));

		// �ltimos comandos da lista, totalmente independente (pode ser executado a
		// qualquer momento)
		this.adicionarComando(new ComandoBot("/voltar", "Retornar para o comando anterior", a -> {
			// Recupera o comando pai (contexto)
			String comandoPaiAnterior = this.getComandoPaiAtual();

			// Caso o comando pai seja o inicial, exibe mensagem dizendo que j� est� no
			// comando inicial
			if (comandoPaiAnterior.toLowerCase().equals("/inicio".toLowerCase())) {
				return "Voc� j� se encontra no comando inicial";
			}

			// Caso ainda n�o esteja no comendo inicial, volta um comando na pilha e exibe
			// mensagem informando para qual comando foi retornado.
			this.voltarUmComandoNaPilha();
			String comandoPaiAtual = this.getComandoPaiAtual();
			return "Voc� retornou para o comando " + comandoPaiAtual;
		}));

		// Instancia a pilha de comandos pai (esp�cie de breadcrumb)
		pilhaComandosPai = new Stack<String>();
		pilhaComandosPai.add(comandoPai);
	}

	public String getHelp() {
		return getHelp(false);
	}

	public String getHelp(boolean forcarRecarregamento) {
		// Retorna a lista de comandos atualizada caso o helpAnterior esteja nulo ou
		// esteja sendo feito o regarregamento for�ado
		if (helpAnterior == null || forcarRecarregamento) {
			return this.retornarStrListaComandos();
		}

		// Caso contr�rio, retorna o helpAnterior
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

	// Retorna todos os comandos na pilha, voltando para o contexto inicial
	public void voltarAoComandoInicialDaPilha() {
		pilhaComandosPai = new Stack<String>();
		pilhaComandosPai.add("/inicio");
	}

	// Exibe a pilha em forma de breadcrumb visual
	public String retornarStrPilha() {
		return pilhaComandosPai.stream().reduce("", (acc, item) -> {
			return String.format("%s > %s", acc, item);
		});
	}

	// Caso o comando seja o /voltar, ou caso o comando seja o comando pai,
	// significa que este comando do par�metro alterou a pilha
	public boolean verificarSeComandoAlterouPilha(String comando) {
		return comando.toLowerCase().equals("/voltar".toLowerCase())
				|| this.getComandoPaiAtual().toLowerCase().equals(comando.toLowerCase());
	}

	// Adiciona v�rios comandos na lista
	public void adicionarComando(List<ComandoBot> listaComandoBot) {
		for (int i = 0; i < listaComandoBot.size(); i++) {
			this.adicionarComando(listaComandoBot.get(i));
		}
	}

	// Adiciona um comando na lista (caso j� tenha algum comando com o mesmo nome,
	// substitui)
	public void adicionarComando(ComandoBot comandoBot) {
		// Antes de adicionar, remove qualquer comando que tenha este nome, para evitar
		// duplicidades
		removerComando(comandoBot.getComando());
		listaTodosComandos.add(comandoBot);
	}

	// Remove um comando da lista
	public void removerComando(String comando) {
		listaTodosComandos.removeIf(c -> c.getComando().toLowerCase().equals(comando.toLowerCase()));
	}

	// Tenta rodar o trecho de c�digo vinculado ao comando, aplicando os par�metros
	// passados
	public String tentarExecutarFuncao(String comando, String parametros) {
		// Caso n�o exista
		if (!this.verificarSeComandoExiste(comando)) {
			// Informa no console que o comando n�o existe
			Console.printarComentario(String.format("Comando %s n�o encontrado", comando));
			return null;
		}

		// Caso contr�rio

		// Busca comando-filho do comando-pai atual
		List<ComandoBot> listaComandosFilhos = this.retornarListaComandosFilhos(comando);

		// Caso o comando n�o seja filho do comando pai atual
		if (listaComandosFilhos.isEmpty()) {
			// Informa no console que o comando n�o est� dispon�vel no momento
			return String.format("Comando %s n�o est� dispon�vel neste ponto da conversa", comando);
		}

		// Caso contr�rio, executa a fun��o atrelada ao comando filho
		return listaComandosFilhos.get(0).executarFuncao(parametros);
	}

	// Verifica se comando buscado existe na lista de comandos
	public boolean verificarSeComandoExiste(String comandoBuscado) {
		Stream<ComandoBot> streamComandosEncontrados = listaTodosComandos.stream().filter(comandoBot -> {
			return comandoBot.getComando().toLowerCase().equals(comandoBuscado.toLowerCase());
		});
		return streamComandosEncontrados.toList().size() > 0;
	}

	// Busca comando na lista de comandos filhos ou na lista de comandos
	// independentes, e retorna os comandos compat�veis
	public List<ComandoBot> retornarListaComandosFilhos(String comandoBuscado) {
		Stream<ComandoBot> streamComandosEncontrados = listaTodosComandos.stream().filter(comandoBot -> {
			return (comandoBuscado == null
					|| comandoBot.getComando().toLowerCase().equals(comandoBuscado.toLowerCase()))
					&& (comandoBot.getComandoPai() == null || comandoBot.getComandoPai().toLowerCase()
							.equals(this.getComandoPaiAtual().toLowerCase()));
		});

		return streamComandosEncontrados.toList();
	}

	// Retorna o comandoPai atual (o Objeto comandoBot, e n�o apenas a String
	// comando)
	public ComandoBot retornarComandoBotPai() {
		String nomeComandoPai = getComandoPaiAtual().toLowerCase();
		return listaTodosComandos.stream()
				.filter(comandoBot -> comandoBot.getComando().toLowerCase().equals(nomeComandoPai)).toList().get(0);
	}

	// Retorna a lista de comandos dispon�veis
	public String retornarStrListaComandos() {
		return this.retornarStrListaComandos("");
	}

	// Retorna a lista de comandos dispon�veis
	public String retornarStrListaComandos(String conteudoResposta) {
		// Retorna array de comandos dispon�veis
		List<ComandoBot> listaComandosFilhosEIndependentes = this.retornarListaComandosFilhos(null);

		// Caso n�o encontre nenhum comando
		if (listaComandosFilhosEIndependentes.size() == 0) {
			return "Nenhum comando encontrado";
		}

		List<ComandoBot> listaComandosIndependentes = listaComandosFilhosEIndependentes.stream()
				.filter(comandoBot -> comandoBot.getComandoPai() == null).toList();

		String breadcrumb = String.format("Voc� est� em: \n%s", this.retornarStrPilha());

		// Inicializa um buffer de strings, para concatenar os comandos dispon�veis
		StringBuilder stringBuilderComandos = new StringBuilder(breadcrumb);

		String conteudoPersonalizadoComandoPai = retornarComandoBotPai().getConteudoPersonalizado();

		// Caso n�o tenha um conte�do personalizado, exibe os comandos filhos e os
		// comandos independentes juntos numa mesma lista
		if (conteudoPersonalizadoComandoPai == null) {

			stringBuilderComandos.append("\n\nComandos dispon�veis:");

			// Adiciona cada um dos comandos (filhos + independentes) no buffer de strings
			for (ComandoBot comandoBot : listaComandosFilhosEIndependentes) {
				stringBuilderComandos.append("\n" + comandoBot.getInfo());

				if (comandoBot.getComando().toLowerCase().equals("/inicio".toLowerCase())
						&& !conteudoResposta.isEmpty()) {
					stringBuilderComandos.append("\n\n" + conteudoResposta);
				}
			}
		} else {
			// Caso tenha um conte�do personalizado, exibe este conte�do no lugar dos
			// comandos filhos e exibe separadamente os comandos independentes
			stringBuilderComandos.append(conteudoPersonalizadoComandoPai);

			stringBuilderComandos.append("\n\nOutros comandos:");

			// Adiciona cada um dos comandos independentes no buffer de strings
			for (ComandoBot comandoBot : listaComandosIndependentes) {
				stringBuilderComandos.append("\n" + comandoBot.getInfo());

				if (comandoBot.getComando().toLowerCase().equals("/inicio".toLowerCase())
						&& !conteudoResposta.isEmpty()) {
					stringBuilderComandos.append("\n\n" + conteudoResposta);
				}
			}
		}

		// Atualiza help anterior, para que na pr�xima requisi��o da lista n�o seja
		// necess�rio rodar toda a busca novamente
		helpAnterior = stringBuilderComandos.toString();

		// Retorna string resultante do buffer
		return helpAnterior;
	}
}
