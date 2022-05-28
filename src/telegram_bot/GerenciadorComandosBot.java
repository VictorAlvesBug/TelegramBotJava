package telegram_bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

// Classe no padrão MonoState:
// Garante que todas as instâncias da classe acessarão aos mesmos atributos estáticos.
public class GerenciadorComandosBot {
	// Lista de comandos aceitos
	private static List<ComandoBot> listaTodosComandos;

	// Pilha de comandos executados em árvore.
	// Similar a uma entrada em diretórios que possuem uma lista de diretórios
	// internos.
	// O contexto muda, então nem todos os comandos disponíveis em um nível estão
	// disponíveis em outro.
	private static Stack<String> pilhaComandosPai;

	// Armazena a última mensagem de help (lista de comandos) enviada para evitar
	// que sejam buscados os comandos disponíveis sendo que são os mesmos
	// previamente exibidos
	private static String helpAnterior = null;

	// Objeto que efetua requisições para a API de filmes e define os comandos
	// disponíveis em relação a este contexto
	private static MoviesApi moviesApi;

	public GerenciadorComandosBot() {
		if (moviesApi == null) {
			Console.printarComentario("Erro ao instanciar GerenciadorComandosBot", true);
			return;
		}
	}

	public GerenciadorComandosBot(String apiKeyMovies) throws Exception {

		// Caso a api de filmes já esteja instanciada, aborta re-execução do construtor
		if (moviesApi != null) {
			return;
		}

		// Inicializa a "fachada" da API de filmes
		moviesApi = new MoviesApi(apiKeyMovies);

		// O comando pai inicial é sempre "/inicio"
		String comandoPai = "/inicio";

		// Instancia a lista de comandos
		listaTodosComandos = new ArrayList<ComandoBot>();

		// Primeiro comando da lista, totalmente independente (pode ser executado a
		// qualquer momento)
		this.adicionarComando(new ComandoBot("/help", "Exibir lista de comandos disponíveis", a -> this.getHelp()));

		// Vai direto para o comando /inicio
		adicionarComando(new ComandoBot("/inicio", "Retornar para o comando inicial", a -> {
			this.voltarAoComandoInicialDaPilha();
			return "Você retornou para o comando inicial";
		}));

		// Adiciona comandos básicos
		this.adicionarComando((new ComandosBasicos()).retornarListaComandos(comandoPai, this));

		// Adiciona comandos com informações dos devs
		this.adicionarComando((new DevsInfo()).retornarListaComandos(comandoPai, this));

		// Adiciona comandos específicos da API de filmes
		this.adicionarComando(moviesApi.retornarListaComandos(comandoPai, this));

		// Adiciona comandos referentes ao jogo da velha (tic-tac-toe)
		this.adicionarComando((new TicTacToeGame()).retornarListaComandos(comandoPai, this));

		// Últimos comandos da lista, totalmente independente (pode ser executado a
		// qualquer momento)
		this.adicionarComando(new ComandoBot("/voltar", "Retornar para o comando anterior", a -> {
			// Recupera o comando pai (contexto)
			String comandoPaiAnterior = this.getComandoPaiAtual();

			// Caso o comando pai seja o inicial, exibe mensagem dizendo que já está no
			// comando inicial
			if (comandoPaiAnterior.toLowerCase().equals("/inicio".toLowerCase())) {
				return "Você já se encontra no comando inicial";
			}

			// Caso ainda não esteja no comendo inicial, volta um comando na pilha e exibe
			// mensagem informando para qual comando foi retornado.
			this.voltarUmComandoNaPilha();
			String comandoPaiAtual = this.getComandoPaiAtual();
			return "Você retornou para o comando " + comandoPaiAtual;
		}));

		// Instancia a pilha de comandos pai (espécie de breadcrumb)
		pilhaComandosPai = new Stack<String>();
		pilhaComandosPai.add(comandoPai);
	}

	public String getHelp() {
		return getHelp(false);
	}

	public String getHelp(boolean forcarRecarregamento) {
		// Retorna a lista de comandos atualizada caso o helpAnterior esteja nulo ou
		// esteja sendo feito o regarregamento forçado
		if (helpAnterior == null || forcarRecarregamento) {
			return this.retornarStrListaComandos();
		}

		// Caso contrário, retorna o helpAnterior
		return helpAnterior;
	}

	// Retorna a posição mais elevada da pilha
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
	// significa que este comando do parâmetro alterou a pilha
	public boolean verificarSeComandoAlterouPilha(String comando) {
		return comando.toLowerCase().equals("/voltar".toLowerCase())
				|| this.getComandoPaiAtual().toLowerCase().equals(comando.toLowerCase());
	}

	// Adiciona vários comandos na lista
	public void adicionarComando(List<ComandoBot> listaComandoBot) {
		for (int i = 0; i < listaComandoBot.size(); i++) {
			this.adicionarComando(listaComandoBot.get(i));
		}
	}

	// Adiciona um comando na lista (caso já tenha algum comando com o mesmo nome,
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

	// Tenta rodar o trecho de código vinculado ao comando, aplicando os parâmetros
	// passados
	public String tentarExecutarFuncao(String comando, String parametros) {
		// Caso não exista
		if (!this.verificarSeComandoExiste(comando)) {
			// Informa no console que o comando não existe
			Console.printarComentario(String.format("Comando %s não encontrado", comando));
			return null;
		}

		// Caso contrário

		// Busca comando-filho do comando-pai atual
		List<ComandoBot> listaComandosFilhos = this.retornarListaComandosFilhos(comando);

		// Caso o comando não seja filho do comando pai atual
		if (listaComandosFilhos.isEmpty()) {
			// Informa no console que o comando não está disponível no momento
			return String.format("Comando %s não está disponível neste ponto da conversa", comando);
		}

		// Caso contrário, executa a função atrelada ao comando filho
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
	// independentes, e retorna os comandos compatíveis
	public List<ComandoBot> retornarListaComandosFilhos(String comandoBuscado) {
		Stream<ComandoBot> streamComandosEncontrados = listaTodosComandos.stream().filter(comandoBot -> {
			return (comandoBuscado == null
					|| comandoBot.getComando().toLowerCase().equals(comandoBuscado.toLowerCase()))
					&& (comandoBot.getComandoPai() == null || comandoBot.getComandoPai().toLowerCase()
							.equals(this.getComandoPaiAtual().toLowerCase()));
		});

		return streamComandosEncontrados.toList();
	}

	// Retorna o comandoPai atual (o Objeto comandoBot, e não apenas a String
	// comando)
	public ComandoBot retornarComandoBotPai() {
		String nomeComandoPai = getComandoPaiAtual().toLowerCase();
		return listaTodosComandos.stream()
				.filter(comandoBot -> comandoBot.getComando().toLowerCase().equals(nomeComandoPai)).toList().get(0);
	}

	// Retorna a lista de comandos disponíveis
	public String retornarStrListaComandos() {
		return this.retornarStrListaComandos("");
	}

	// Retorna a lista de comandos disponíveis
	public String retornarStrListaComandos(String conteudoResposta) {
		// Retorna array de comandos disponíveis
		List<ComandoBot> listaComandosFilhosEIndependentes = this.retornarListaComandosFilhos(null);

		// Caso não encontre nenhum comando
		if (listaComandosFilhosEIndependentes.size() == 0) {
			return "Nenhum comando encontrado";
		}

		List<ComandoBot> listaComandosIndependentes = listaComandosFilhosEIndependentes.stream()
				.filter(comandoBot -> comandoBot.getComandoPai() == null).toList();

		String breadcrumb = String.format("Você está em: \n%s", this.retornarStrPilha());

		// Inicializa um buffer de strings, para concatenar os comandos disponíveis
		StringBuilder stringBuilderComandos = new StringBuilder(breadcrumb);

		String conteudoPersonalizadoComandoPai = retornarComandoBotPai().getConteudoPersonalizado();

		// Caso não tenha um conteúdo personalizado, exibe os comandos filhos e os
		// comandos independentes juntos numa mesma lista
		if (conteudoPersonalizadoComandoPai == null) {

			stringBuilderComandos.append("\n\nComandos disponíveis:");

			// Adiciona cada um dos comandos (filhos + independentes) no buffer de strings
			for (ComandoBot comandoBot : listaComandosFilhosEIndependentes) {
				stringBuilderComandos.append("\n" + comandoBot.getInfo());

				if (comandoBot.getComando().toLowerCase().equals("/inicio".toLowerCase())
						&& !conteudoResposta.isEmpty()) {
					stringBuilderComandos.append("\n\n" + conteudoResposta);
				}
			}
		} else {
			// Caso tenha um conteúdo personalizado, exibe este conteúdo no lugar dos
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

		// Atualiza help anterior, para que na próxima requisição da lista não seja
		// necessário rodar toda a busca novamente
		helpAnterior = stringBuilderComandos.toString();

		// Retorna string resultante do buffer
		return helpAnterior;
	}
}
