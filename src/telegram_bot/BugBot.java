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

	// Criando objeto "bot" que ser� respons�vel por enviar e receber as mensagens
	private TelegramBot bot;

	// Armazena o id da �ltima mensagem lida, para fazer offset ao ler as mensagens
	// pendentes
	private int idMensagemJaLida;

	// Construtor recebe as chaves de acesso via par�metro
	public BugBot(String tokenTelegramBot, String apiKeyMovies) {

		// Caso n�o seja informado o token, lan�a exce��o
		if(tokenTelegramBot == null || tokenTelegramBot.isEmpty())
		{
			return;
		}
		
		// Adicionando informa��es de acesso ao bot
		bot = new TelegramBot(tokenTelegramBot);

		// Inicializando o id da �ltima mensagem lida como zero, para que ao iniciar a
		// conversa todas as mensagens sejam consideradas como n�o-lidas
		idMensagemJaLida = 0;

		// Define quais s�o os comandos dispon�veis no bot
		GerenciadorComandosBot.getInstancia(apiKeyMovies);
	}

	// M�todo executado em loop, para verificar se h� alguma mensagem recebida do usu�rio
	public void atualizar() {
		
		// Retorna apenas as mensagem recebidas por �ltimo e ainda n�o analizadas
		List<Update> listaMensagensRecebidas = bot.execute(new GetUpdates().limit(100).offset(idMensagemJaLida))
				.updates();

		// Caso n�o tenha nenhuma mensagem pendente, aborta a execu��o do m�todo
		if (listaMensagensRecebidas == null) {
			return;
		}

		// Analiza cada mensagem recebida
		for (Update mensagemRecebida : listaMensagensRecebidas) {
			
			// Incrementa id da �ltima mensagem lida, para que ela seja desconsiderada 
			// na pr�xima itera��o.
			idMensagemJaLida = mensagemRecebida.updateId() + 1;

			// Interpreta mensagem recebida e define uma resposta adequada.
			tratarMensagemRecebida(mensagemRecebida);
		}
	}

	private void tratarMensagemRecebida(Update mensagem) {
		// Caso a mensagem n�o esteja dispon�vel, aborta a execu��o do m�todo
		if (mensagem == null || mensagem.message() == null || mensagem.message().chat() == null
				|| mensagem.message().chat().id() == null || mensagem.message().text() == null) {
			return;
		}

		// Armazena o id do chat que enviou as mensagens para o bot, para 
		// futuramente responder ao chat correto
		Long chatId = mensagem.message().chat().id();
		
		// Armazena apenas o comando recebido
		String strComando = mensagem.message().text().trim();

		// Exibe no console o comando recebido
		Console.printarMensagemRecebida(strComando);

		// Envia "Digitando..." antes de enviar a resposta
		enviarAvisoDigitando(chatId);

		// Envia a resposta de acordo com o comando recebido
		responderComando(chatId, strComando);
	}

	private void enviarAvisoDigitando(Long chatId) {
		
		// Envia "Digitando..."
		String digitando = ChatAction.typing.name();
		boolean avisoDigitandoFoiEnviado = bot.execute(new SendChatAction(chatId, digitando)).isOk();

		// Exibe no console se "Digitando..." foi enviado com sucesso ou n�o
		printarStatusMensagemEnviada(avisoDigitandoFoiEnviado, digitando);
	}

	private void responderComando(Long chatId, String comando) {
		// Retorna a resposta que ser� enviada na conversa
		String listaRespostas = retornarRespostas(comando);
		
		// Envia resposta
		boolean enviou = bot.execute(new SendMessage(chatId, listaRespostas)).isOk();

		// Exibe no console se a resposta foi enviada com sucesso ou n�o
		printarStatusMensagemEnviada(enviou, listaRespostas);
	}

	private String retornarRespostas(String strComandos) {
		
		// Inicializa um buffer de strings, para possibilitar o ac�mulo de respostas em
		// uma s� mensagem
		StringBuilder sbRespostas = new StringBuilder();

		// Armazena a lista de comandos recebidos no seguinte formato:
		// [ 
		//   "/comandoSemParametro", 
		//   "/comandoComUmParametro;parametroUnico", 
		//   "/comandoComDoisParametros;parametro1;Par�metro com espa�o"
		// ]
		List<String> listaComandosRecebidos = retornarListaComandosRecebidos(strComandos);

		// Caso nenhum comando seja reconhecido, aborta a execu��o, informando que os comandos 
		// n�o foram encontrados
		if (listaComandosRecebidos == null || listaComandosRecebidos.isEmpty()) {
			// Exibe "N�o entendi..."
			sbRespostas.append("N�o entendi...");
			// E informa quais s�o os comandos dispon�veis
			sbRespostas.append("\n\n" + GerenciadorComandosBot.getInstancia().getHelp());
			return sbRespostas.toString();
		}

		// Itera cada um dos comandos e executa sua fun��o atrelada
		for (String comandoRecebido : listaComandosRecebidos) {
			int indicePrimeiroSeparador = comandoRecebido.indexOf(";");
			
			// Caso n�o encontre o primeiro separador, a vari�vel armazenar� -1.
			// Desta forma, o comando � chamado sem par�metros
			if (indicePrimeiroSeparador == -1) {
				sbRespostas.append(GerenciadorComandosBot.getInstancia().tentarExecutarFuncao(comandoRecebido, null));
			} 
			else {
				// Caso o primeiro separador exista, separa o primeiro trecho (comando) do resto (par�metros)
				String comando = comandoRecebido.substring(0, indicePrimeiroSeparador);
				String strParametros = comandoRecebido.substring(indicePrimeiroSeparador + 1);
				sbRespostas.append(GerenciadorComandosBot.getInstancia().tentarExecutarFuncao(comando, strParametros));
			}
		}
		
		// Flag que sinaliza se pelo menos um comando executado alterou a pilha de comandos pai.
		boolean algumComandoAlterouPilha = false;
		
		// Itera em cada conjunto comandoComParametro para verificar se algum deles alterou a pilha de comandos pai.
		for(String comandoRecebido : listaComandosRecebidos) {
			
			// Recupera o comando, sem nenhum par�metro
			String comandoSemParametros = comandoRecebido;
			int indicePrimeiroSeparador = comandoRecebido.indexOf(";");
			if (indicePrimeiroSeparador != -1) {
				comandoSemParametros = comandoSemParametros.substring(0, indicePrimeiroSeparador);
			} 
			
			// Caso o comando sem par�metro seja o "/inicio" ou seja o comando pai mais pr�ximo na pilha,
			// � entendido que a execu��o do comando alterou a pilha. 
			if (GerenciadorComandosBot.getInstancia().verificarSeComandoAlterouPilha(comandoSemParametros)) {
				algumComandoAlterouPilha = true;
				// Nem precisa continuar procurando, pois o comando atual j� sinalizou que alterou a pilha
			}
		}
		
		// Caso tenha algum comando que tenha alterado a pilha, a lista de comandos poss�veis,
		// deve ter sido alterada, ent�o exibe a lista de comandos atualizada.
		if (algumComandoAlterouPilha) {
			return GerenciadorComandosBot.getInstancia().retornarStrListaComandos(sbRespostas.toString());
		}

		return sbRespostas.toString();
	}

	private List<String> retornarListaComandosRecebidos(String strComandos) {
		 
		// Contador de aspas para saber se o caracter " " (espa�o) � um separador de trechos 
		// ou apenas um caracter de texto
		int contadorAspas = 0;
		
		// Buffer respons�vel por armazenar cada caracter at� que o trecho esteja completo
		StringBuilder sbTrecho = new StringBuilder();
		// Lista de trechos encontrados
		List<String> listaTrechos = new ArrayList<String>();

		// o "for" abaixo itera cada um dos caracteres para alterar o formato:
		
		// Ex1 (antes):  /comandoSemParametro 
		// Ex1 (depois): ["/comandoSemParametro"]
		
		// Ex2 (antes):  /comandoComUmParametro parametroUnico
		// Ex2 (depois): ["/comandoComUmParametro", "parametroUnico"]
		
		// Ex3 (antes):  /comandoComDoisParametros parametro1 "Par�metro com espa�o"
		// Ex3 (depois): ["/comandoComDoisParametros", "parametro1", "Par�metro com espa�o"]
		
		// Ex4 (antes):  /comando1 param1 /comando2 "param2 com espa�o"
		// Ex4 (depois): ["/comando1", "param1", "/comando2", "param2 com espa�o"]
		
		for (char caracter : strComandos.toCharArray()) {
			switch (caracter) {
			// Aspas, usadas para delimitar uma string com " " (espa�os) e evitar que seja 
			// confundido com um " " (espa�o) de separa��o entre trechos.
			// Observa��o: est� l�gica trata ambos os tipo de aspas (' e ") da mesma forma,
			// sendo poss�vel abrir um trecho de texto com um tipo e fechar com outro.
			// Ex: /bugFeatureDasAspas 'este � um par�metro"
			case '"':
			case '\'':
				// Incrementa o n�mero de aspas encontradas at� ent�o.
				contadorAspas++;
				break;

			// Espa�o, caso n�o esteja dentro de aspas, finaliza o trecho atual e usa o " " (espa�o)
			// como separador de trechos.
			case ' ':
				// Entende-se que o conjunto de caracteres que est� entre uma aspa �mpar e uma par 
				// � apenas texto.
				// Desta forma, se o n�mero de aspas encontradas at� ent�o for par, significa que o 
				// " " (espa�o) encontrado � realmente um separador de trechos.
				// Por�m, se o n�mero de aspas encontradas for impar, significa que o " " (espa�o) 
				// encontrado � parte de um texto e n�o deve ser considerado como um separador de trechos.
				if (contadorAspas % 2 == 0) {
					// " " � um separador de trechos, ent�o adiciona o trecho na lista e limpa a vari�vel
					listaTrechos.add(sbTrecho.toString());
					sbTrecho = new StringBuilder();
				} else {
					// " " � apenas um caracter de texto, ent�o apenas adiciona o caracter ao trecho
					sbTrecho.append(caracter);
				}
				break;

			// Demais caracteres, s�o apenas adicionados ao trecho atual.
			default:
				sbTrecho.append(caracter);
			}
		}
		
		// O que adicionou os trecho at� o pen�ltimo foi o " " (espa�o) que veio logo depois. 
		// Como o �ltimo trecho n�o possui um " " (espa�o) depois, ele deve ser adicionado separadamente.
		if(sbTrecho.toString().length() > 0){
			listaTrechos.add(sbTrecho.toString());
		}
		
		// Buffer que guarda cada caracter do conjunto comando-par�metro antes de adicionar na lista.
		StringBuilder sbComandoComParametro = new StringBuilder();
		// Lista de conjuntos comando-parametro.
		List<String> listaComandosComParametros = new ArrayList<String>();

		// o "for" abaixo itera cada um dos caracteres para alterar o formato:
		
		// Ex1 (antes):  ["/comandoSemParametro"]
		// Ex1 (depois): ["/comandoSemParametro"]
		
		// Ex2 (antes):  ["/comandoComUmParametro", "parametroUnico"]
		// Ex2 (depois): ["/comandoComUmParametro;parametroUnico"]
		
		// Ex3 (antes):  ["/comandoComDoisParametros", "parametro1", "Par�metro com espa�o"]
		// Ex3 (depois): ["/comandoComDoisParametros;parametro1;Par�metro com espa�o"]
		
		// Ex4 (antes):  /comando1 param1 /comando2 "param2 com espa�o"
		// Ex4 (depois): ["/comando1;param1", "/comando2;param2 com espa�o"]
		
		for (String trecho : listaTrechos) {
			
			// Remove qualquer ";", pois este caracter ser� utilizado como separa��o entre os trechos
			trecho = trecho.replace(";", "");
			
			// Verifica se o trecho � reconhecido como um comando v�lido
			if(GerenciadorComandosBot.getInstancia().verificarSeComandoExiste(trecho)) {
				
				// Caso tenha algo concatenado no comandoComParametro, adiciona este "algo" na lista
				// Este "algo" pode ser um comando simples ou um comando com par�metros.
				// Mas como o "trecho" � considerado como um comando, corta o trecho anterior e o adiciona na lista
				if (sbComandoComParametro.toString().length() > 0) {
					listaComandosComParametros.add(sbComandoComParametro.toString());
				}
				
				// Reinicializa o comandoComParametro com o trecho atual.
				// Todo comandoComParametro � inciado com um comando v�lido.
				sbComandoComParametro = new StringBuilder(trecho);
				
			}
			else {
				// Se o comandoComParametro n�o est� preenchido significa que o primeiro trecho n�o � um comando v�lido.
				// Desta forma, aborta a execu��o do m�todo, pois o primeiro trecho precisava ser um comando v�lido.
				if(sbComandoComParametro.toString().isEmpty()) {
					return null;
				}

				// Caso o comandoComParametro tenha algo dentro, concatena um separador ";" e o "trecho" atual;
				sbComandoComParametro.append(";" + trecho);
			}
			
		}
		
		// Da mesma forma que o "if" acima, no mesmo n�vel de identa��o (fora do "for"):
		// O que adicionou os trecho at� o pen�ltimo foi o comando v�lido que veio logo depois. 
		// Como o �ltimo trecho n�o possui um comando depois, ele deve ser adicionado separadamente.
		if(sbComandoComParametro.toString().length() > 0){
			listaComandosComParametros.add(sbComandoComParametro.toString());
		}
		
		return listaComandosComParametros;
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
