package telegram_bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

// Classe no padrão MonoState:
// Garante que todas as instâncias da classe acessarão aos mesmos atributos estáticos.
public class GerenciadorComandosBot {

	// Lista de comandos aceitos
	private static List<ComandoBot> listaTodosComandos;

	// Pilha de comandos executados em arvore.
	// Similar a uma entrada em diretórios que possuem uma lista de diretórios
	// internos.
	// O contexto muda, então nem todos os comandos disponíveis em um nível estão
	// disponíveis em outro.
	private static Stack<String> pilhaComandosPai;

	// Armazena a última mensagem de help (lista de comandos) enviada para evitar
	// ficar buscando entre
	// os comandos disponíveis no momento
	private static String helpAnterior = null;

	// Objeto que efetua requisições para a api de filmes e define os comandos
	// disponíveis em relação a
	// este contexto
	private static MoviesApi moviesApi;
	

	
	public GerenciadorComandosBot() {
		if(moviesApi == null)
		{
			Console.printarComentario("Erro ao instanciar GerenciadorComandosBot", true);
			return;
		}
	}
	
	public GerenciadorComandosBot(String apiKeyMovies) throws Exception {

		if(moviesApi != null)
		{
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

		// Adiciona comandos básicos
		this.adicionarComando((new ComandosBasicos()).retornarListaComandos(comandoPai, this));

		// Adiciona comandos específicos da API de filmes
		this.adicionarComando(moviesApi.retornarListaComandos(comandoPai, this));

		// Últimos comandos da lista, totalmente independente (pode ser executado a
		// qualquer momento)
		this.adicionarComando(new ComandoBot("/voltar", "Retornar para o comando anterior", a -> {
			// Recupera o comando pai (contexto)
			String comandoPaiAnterior = this.getComandoPaiAtual();

			// Caso o comando pai seja o inicial, exibe mensagem dizendo que já está no
			// comando inicial
			if (comandoPaiAnterior.equals("/inicio")) {
				return "Você já se encontra no comando inicial";
			}

			// Caso ainda não esteja no comendo inicial, volta um comando na pilha e exibe
			// mensagem informando para qual comando foi retornado.
			this.voltarUmComandoNaPilha();
			String comandoPaiAtual = this.getComandoPaiAtual();
			return "Você retornou para o comando " + comandoPaiAtual;
		}));

		// Vai direto para o comando inicio
		adicionarComando(new ComandoBot("/inicio", "Retornar para o comando início", a -> {
			this.voltarAoComandoInicialDaPilha();
			return "Você retornou para o comando inicial";
		}));

		// Instancia a pilha de comandos pai
		pilhaComandosPai = new Stack<String>();
		pilhaComandosPai.add(comandoPai);
	}

	public String getHelp() {
		// Retorna o help (lista de comandos) anterior caso não esteja nula
		if (helpAnterior == null) {
			return this.retornarStrListaComandos();
		}

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
		return comando.equals("/voltar") || this.getComandoPaiAtual().equals(comando);
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
		listaTodosComandos.removeIf(c -> c.getComando().equals(comandoBot.getComando()));
		listaTodosComandos.add(comandoBot);
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

		// Busca comando-filho do comando-pai atual
		List<ComandoBot> listaComandosFilhos = this.retornarListaComandosFilhos(comando);

		// Caso o comando não seja filho do comando pai atual
		if (listaComandosFilhos.isEmpty()) {
			// Informa no console que o comando não está disponível no momento
			return String.format("Comando %s não está disponível neste ponto da conversa", comando);
		}

		// executa a função atrelada ao comando filho
		// TODO: Permitir a passagem de informações por argumento
		return listaComandosFilhos.get(0).executarFuncao(parametros);
	}

	// Verifica se comando buscado na lista de comandos disponíveis
	public boolean verificarSeComandoExiste(String comandoBuscado) {
		Stream<ComandoBot> streamComandosEncontrados = listaTodosComandos.stream().filter(comandoBot -> {
			return comandoBot.getComando().equals(comandoBuscado);
		});
		return streamComandosEncontrados.toList().size() > 0;
	}

	// Busca comando na lista de comandos filhos ou na lista de comandos
	// independentes, e retorna o comando compatível
	public List<ComandoBot> retornarListaComandosFilhos(String comandoBuscado) {
		Stream<ComandoBot> streamComandosEncontrados = listaTodosComandos.stream().filter(comandoBot -> {
			return (comandoBuscado == null || comandoBot.getComando().equals(comandoBuscado))
					&& (comandoBot.getComandoPai() == null || comandoBot.getComandoPai().equals(this.getComandoPaiAtual()));
		});

		return streamComandosEncontrados.toList();
	}

	public String retornarStrListaComandos() {
		return this.retornarStrListaComandos("");
	}

	public String retornarStrListaComandos(String conteudoResposta) {
		// Retorna array de comandos disponíveis
		List<ComandoBot> listaComandosFilhos = this.retornarListaComandosFilhos(null);

		// Caso não encontre nenhum comando
		if (listaComandosFilhos.size() == 0) {
			return "Nenhum comando encontrado";
		}

		String breadcrumb = String.format("Você está em: \n%s", this.retornarStrPilha());

		// Inicializa um buffer de strings, para concatenar os comandos disponíveis
		StringBuilder stringBuilderComandos = new StringBuilder(breadcrumb);
		stringBuilderComandos.append("\n\nComandos disponíveis:");

		// Adiciona cada um dos comandos no buffer de strings
		for (ComandoBot comandoBot : listaComandosFilhos) {
			stringBuilderComandos.append("\n" + comandoBot.getInfo());

			if (comandoBot.getComando().equals("/inicio") && !conteudoResposta.isEmpty()) {
				stringBuilderComandos.append("\n\n" + conteudoResposta);
			}
		}

		// Atualiza help anterior, para que na próxima requisição da lista não seja
		// necessário rodar toda a busca novamente
		helpAnterior = stringBuilderComandos.toString();

		// Retorna string resultante do buffer
		return helpAnterior;
	}
}
