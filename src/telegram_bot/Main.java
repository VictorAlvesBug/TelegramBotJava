package telegram_bot;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {

	/*
	 	Para limpar o cache, clicar com o botão direito no console do Eclipse e selecionar a opção "Terminate/Disconnect All"
	 	
	 TODO:
	 	Corrigir regex de saudação
	 	Integrar API de filmes
	 	Integrar API do IBM Watson
	 	Integrar API de Pokedex
	 	Integrar API de cotações de ações da bolsa de valores
	 */
	
	public static void main(String[] args) throws IOException {
		// Configurando acesso ao arquivo de variáveis globais
		Properties prop = new Properties();
		FileInputStream ip = new FileInputStream("config.properties");
		prop.load(ip);

		String tokenTelegramBot = prop.getProperty("token_telegram_bot");
		String apiKeyMovies = prop.getProperty("api_key_movies");
		
		// Criando um objeto do tipo BugBot
		BugBot bugBot = new BugBot(tokenTelegramBot, apiKeyMovies);

		// Loop infinito pode ser alterado por algum timer de intervalo curto.
		while (true) {
			bugBot.atualizar();
		}
	}
}


/*

Anotações:

OK - Armazenar funções principais numa classe separada (ComandosBasicos.java)
OK - Armazenar funções relacionadas a uma API dentro da classe que acessa a API.
- Possibilitar execução de múltiplos comandos em série 
Ex: "/alfanumericos /numeros /..."
- Permitir inclusão de parâmetros:
Ex: "/buscarfilmes game"
Ex: "/buscarfilmes 'toy st'"
// Caso não seja saudação
// Roda em cada caracter da mensagem e vai concatenando em um comando aux.
// Ao encontrar um " ", adiciona comando na lista e limpa o comando aux.
// Caso o numero de aspas encontradas ate o caracter seja ímpar, não quebra o comando no " ", pois o caracter esta dentro de um par de aspas.

// Configurar para funcionar com /x  sendo x o numero do comando na lista de comandos disponíveis, iniciando em zero.

Ao buscar comando
String comandoSemBarra = comando.substring(1);
if(comandoSemBarra.isnumeric()){
    int numComando = (int) comandoSemBarra;
    List<ComandoBot> listaComandosFilhos = retornarListaComandosFilhos();
    if(numComando < listaComandosFilhos.size()){
        listaComandosFilhos.get(numComando).executarFuncao(parametros...);
    }
}

*/