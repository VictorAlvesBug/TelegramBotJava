package telegram_bot;

// Interface respons�vel por permitir que seja poss�vel utilizar fun��es lambda na chamada 
// do construtor da classe ComandoBot
public interface IFuncao {
	public String executar(Object parametro);
}
