package telegram_bot;

public class ComandoBot {
	
	private String comando;
	private String descricao;
	private IFuncao funcao;
	
	private String comandoPai;
	
	private Object objInformacoesAdicionais;

	public ComandoBot(String comando, String descricao, IFuncao funcao) {
		// Quando um comando não possui um comando pai (nem mesmo o /inicio) ele é um comando que pode ser executado a qualquer momento
		this(comando, descricao, funcao, null);
	}

	public ComandoBot(String comando, String descricao, IFuncao funcao, String comandoPai) {
		// Armazena demais atributos informados pelos parâmetros
		this.comando = comando;
		this.descricao = descricao;
		this.funcao = funcao;
		
		/*
		comandoPai = null --> comando pode ser executado a qualquer momento
		comandoPai = {não nulo} --> comando só pode ser executado quando estiver neste contexto
		*/
		this.comandoPai = comandoPai;
	}

	public String getComando() {
		return this.comando;
	}

	public String getInfo() {
		// Retorna informações do comando
		// Ex: "/help - Exibir lista de comandos disponíveis"
		return String.format("%s - %s", this.comando, this.descricao);
	}
	
	public String getComandoPai() {
		return this.comandoPai;
	}

	public String executarFuncao(String parametro) {
		// Executa função atribuída a este comando
		if(parametro == null)
		{
			return this.funcao.executar(objInformacoesAdicionais);
		}
		return this.funcao.executar(parametro);
	}
	
	public void setObjInformacoesAdicionais(Object obj) {
		objInformacoesAdicionais = obj;
	}
}
