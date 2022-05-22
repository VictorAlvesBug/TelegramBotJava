package telegram_bot;

// Interface responsável por permitir que seja possível utilizar funções lambda na chamada 
// do construtor da classe ComandoBot
public interface IFuncao {
	public String executar(Object parametro);
}
