package telegram_bot;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {

	/*
	 	Para limpar o cache, clicar com o bot�o direito no console do Eclipse e selecionar a op��o "Terminate/Disconnect All"
	 	
	 TODO:
	 	Corrigir regex de sauda��o
	 	Integrar API de filmes
	 	Integrar API do IBM Watson
	 	Integrar API de Pokedex
	 	Integrar API de cota��es de a��es da bolsa de valores
	 */
	
	public static void main(String[] args) throws IOException {
		// Configurando acesso ao arquivo de vari�veis globais
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
