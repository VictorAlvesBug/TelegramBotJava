package telegram_bot;

import java.util.ArrayList;
import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;

public class BugBot {

	// Cria��o do objeto bot
	private TelegramBot bot;

	// Armazena o id da �ltima mensagem lida, para fazer offset ao ler as mensagens
	// pendentes
	private int idMensagemJaLida;

	public BugBot(String tokenTelegramBot, String apiKeyMovies) {
		// Adicionando informa��es de acesso ao bot
		bot = new TelegramBot(tokenTelegramBot);

		// Inicializando o id da �ltima mensagem lida como zero, para que ao iniciar a
		// conversa todas as mensagens sejam consideradas
		idMensagemJaLida = 0;

		// Define quais s�o os comandos dispon�veis no bot
		GerenciadorComandosBot.inicializar(apiKeyMovies);
	}

	public void atualizar() {
		// Retorna apenas as mensagem recebidas por �ltimo e ainda n�o analizadas
		List<Update> listaMensagensRecebidas = bot.execute(new GetUpdates().limit(100).offset(idMensagemJaLida))
				.updates();

		// Caso n�o tenha nenhuma mensagem pendente, n�o analiza nenhuma mensagem
		if (listaMensagensRecebidas == null) {
			return;
		}

		// Analiza cada mensagem recebida
		for (Update mensagemRecebida : listaMensagensRecebidas) {
			// Incrementa id da �ltima mensagem lida, para que ela seja desconsiderada na
			// pr�xima itera��o.
			idMensagemJaLida = mensagemRecebida.updateId() + 1;

			// Interpreta mensagem recebida e define uma resposta adequada.
			tratarMensagemRecebida(mensagemRecebida);
		}
	}

	private void tratarMensagemRecebida(Update mensagem) {
		// Caso a mensagem n�o esteja dispon�vel, retorna para o m�todo que chamou
		if (mensagem == null || mensagem.message() == null || mensagem.message().chat() == null
				|| mensagem.message().chat().id() == null || mensagem.message().text() == null) {
			return;
		}

		Long chatId = mensagem.message().chat().id();
		String strMensagem = mensagem.message().text().trim();

		// Exibe no console a mensagem recebida
		Console.printarMensagemRecebida(strMensagem);

		// Envia 'Digitando...' antes de enviar a resposta
		enviarAvisoDigitando(chatId);

		// Envia a resposta
		responderMensagem(chatId, strMensagem);
	}

	private void enviarAvisoDigitando(Long chatId) {
		// Envia 'Digitando...'
		String digitando = ChatAction.typing.name();
		boolean avisoDigitandoFoiEnviado = bot.execute(new SendChatAction(chatId, digitando)).isOk();

		// Exibe no console se 'Digitando...' foi enviado com sucesso ou n�o
		printarStatusMensagemEnviada(avisoDigitandoFoiEnviado, digitando);
	}

	private void responderMensagem(Long chatId, String mensagem) {
		// Retorna a resposta que ser� enviada na conversa
		String listaRespostas = retornarRespostas(mensagem);
		
		// Envia resposta
		boolean enviou = bot.execute(new SendMessage(chatId, listaRespostas)).isOk();

		// Exibe no console se a resposta foi enviada com sucesso ou n�o
		printarStatusMensagemEnviada(enviou, listaRespostas);
	}

	private String retornarRespostas(String mensagem) {
		// Inicializa um buffer de strings, para possibilitar o ac�mulo de respostas em
		// uma s� mensagem
		StringBuilder sbRespostas = new StringBuilder("");

		// Caso a mensagem seja uma sauda��o
		if (ehSaudacao(mensagem)) {
			// Responde sauda��o
			sbRespostas.append(retornarRespostaSaudacao());
			// E informa quais s�o os comandos dispon�veis
			sbRespostas.append("\n\n" + GerenciadorComandosBot.getHelp());
			return sbRespostas.toString();
		}

		List<String> listaComandosRecebidos = retornarListaComandosRecebidos(mensagem);

		if (listaComandosRecebidos == null || listaComandosRecebidos.isEmpty()) {
			// Exibe 'N�o entendi...'
			sbRespostas.append("N�o entendi...");
			// E informa quais s�o os comandos dispon�veis
			sbRespostas.append("\n\n" + GerenciadorComandosBot.getHelp());
			return sbRespostas.toString();
		}

		for (String comandoRecebido : listaComandosRecebidos) {
			int indicePrimeiroSeparador = comandoRecebido.indexOf(";");
			if (indicePrimeiroSeparador == -1) {
				sbRespostas.append(GerenciadorComandosBot.tentarExecutarFuncao(comandoRecebido, null));
			} 
			else {
				String comando = comandoRecebido.substring(0, indicePrimeiroSeparador);
				String strParametros = comandoRecebido.substring(indicePrimeiroSeparador + 1);
				sbRespostas.append(GerenciadorComandosBot.tentarExecutarFuncao(comando, strParametros));
			}
		}

		String comandoSemParametros = mensagem;
		int indicePrimeiroEspaco = mensagem.indexOf(" ");
		if (indicePrimeiroEspaco != -1) {
			comandoSemParametros = comandoSemParametros.substring(0, indicePrimeiroEspaco);
		} 
		
		if (GerenciadorComandosBot.verificarSeComandoAlterouPilha(comandoSemParametros)) {
			return GerenciadorComandosBot.retornarStrListaComandos(sbRespostas.toString());
		}

		return sbRespostas.toString();
	}

	private List<String> retornarListaComandosRecebidos(String mensagem) {
		int contadorAspas = 0;
		StringBuilder sbTrecho = new StringBuilder("");
		List<String> listaTrechos = new ArrayList<String>();

		for (char caracter : mensagem.toCharArray()) {
			switch (caracter) {
			// aspas
			case '"':
			case '\'':
				contadorAspas++;
				break;

			// espa�o
			case ' ':
				if (contadorAspas % 2 == 0) {
					listaTrechos.add(sbTrecho.toString());
					sbTrecho = new StringBuilder("");
				} else {
					sbTrecho.append(caracter);
				}
				break;

			// demais caracteres
			default:
				sbTrecho.append(caracter);
			}
		}
		
		// Ultimo deve ser adicionado separadamente, 
		// pois o que acabou adicionando os outros � o fato de que eles n�o eram o ultimo
		if(sbTrecho.toString().length() > 0){
			listaTrechos.add(sbTrecho.toString());
		}
		
		StringBuilder sbComandoComParametro = new StringBuilder("");
		List<String> listaComandosComParametros = new ArrayList<String>();
		
		for (String trecho : listaTrechos) {
			trecho = trecho.replace(";", "");
			
			if(GerenciadorComandosBot.verificarSeComandoExiste(trecho)) {
				
				if (sbComandoComParametro.toString().length() > 0) {
					listaComandosComParametros.add(sbComandoComParametro.toString());
				}
				
				sbComandoComParametro = new StringBuilder(trecho);
				
			}
			else {
				if(sbComandoComParametro.toString().isEmpty()) {
					return null;
				}

				sbComandoComParametro.append(";");
				sbComandoComParametro.append(trecho);
			}
			
		}
		
		// Ultimo deve ser adicionado separadamente, 
		// pois o que acabou adicionando os outros � o fato de que eles n�o eram o ultimo
		if(sbComandoComParametro.toString().length() > 0){
			listaComandosComParametros.add(sbComandoComParametro.toString());
		}
		
		return listaComandosComParametros;
	}

	// M�todo que identifica se a mensagem recebida � uma sauda��o
	private boolean ehSaudacao(String mensagem) {
		String regexSaudacao = "//((ol[a�]+)|(oi+e*)|(op+a+)|(ob+a+)|((bom|boa)\\s(dia|tarde|noite|madrugada))).*//";
		return mensagem.toLowerCase().matches(regexSaudacao);
	}

	private String retornarRespostaSaudacao() {
		// Define quais s�o as sauda��es dispon�veis
		String[] listaSaudacoesResposta = new String[] { "Bom dia!", "Boa tarde!", "Boa noite!", "Ol�, tudo bom?" };

		// Sorteia uma sauda��o dentre as dispon�veis e retorna
		int indice = (int) Math.floor(Math.random() * listaSaudacoesResposta.length);
		return listaSaudacoesResposta[indice];
	}

	private void printarStatusMensagemEnviada(boolean enviou, String mensagem) {
		// Caso a mensagem tenha sido enviada com sucesso
		if (enviou == true) {
			// Exibe a mensagem no console
			Console.printarMensagemEnviada(mensagem);
			return;
		}

		// Informa erro ao enviar mensagem
		Console.printarErroAoEnviarMensagem(mensagem);
	}
}
