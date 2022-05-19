package telegram_bot;

import java.util.function.Function;

public class ComandoBot implements Comparable<ComandoBot> {

	private int indice;
	private static int maxIndice = 0;
	
	private String comando;
	private String descricao;
	private Function<String, String> funcao;
	
	private String comandoPai;

	public ComandoBot(String comando, String descricao, Function<String, String> funcao) {
		// Quando um comando n�o possui um comando pai (nem mesmo o /inicio) ele � um comando que pode ser executado a qualquer momento
		this(comando, descricao, funcao, null);
	}

	public ComandoBot(String comando, String descricao, Function<String, String> funcao, String comandoPai) {
		// Incrimenta um no �ndice m�ximo
		maxIndice++;
		// Atribui valor ao indice do objeto atual
		this.indice = maxIndice;

		// Armazena demais atributos informados pelos par�metros
		this.comando = comando;
		this.descricao = descricao;
		this.funcao = funcao;
		
		/*
		comandoPai = null --> comando pode ser executado a qualquer momento
		comandoPai = {n�o nulo} --> comando s� pode ser executado quando estiver neste ponto da conversa
		*/
		this.comandoPai = comandoPai;
	}

	public int getIndice() {
		return this.indice;
	}

	public String getComando() {
		return this.comando;
	}

	public String getInfo() {
		// Retorna informa��es do comando
		// Ex: "/help - Exibir lista de comandos dispon�veis"
		return String.format("%s - %s", this.comando, this.descricao);
	}
	
	public String getComandoPai() {
		return this.comandoPai;
	}

	public String executarFuncao(String parametro) {
		// Executa fun��o atribu�da a este comando
		return this.funcao.apply(parametro);
	}

	@Override
	public int compareTo(ComandoBot outraInstancia) {
		/*
		Positivo 	--> a inst�ncia atual possue maior �ndice
		Zero 		--> ambas as inst�ncias possuem o mesmo �ndice
		Negativo 	--> a outra inst�ncia possue maior �ndice
		*/
		return - this.indice + outraInstancia.getIndice();/* invertido temporariamente*/
	}
}
