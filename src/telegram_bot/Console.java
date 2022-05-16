package telegram_bot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Console {
	// Armazena a última data que foi exibida, para que não seja exibida novamente caso seja a mesma
	private static String strUltimaDataExibida = "";
	// Armazena a última hora que foi exibida, para que não seja exibida novamente caso seja a mesma (e seja a mesma data)
	private static String strUltimaHoraExibida = "";

	public static void printarMensagemEnviada(String mensagem) {
		printarDataHoraAtual();
		// Exibe no console a mensagem enviada
		System.out.println(String.format("--> (enviado)\n%s", mensagem));
	}

	public static void printarErroAoEnviarMensagem(String mensagem) {
		printarDataHoraAtual();
		// Informa erro no console ao enviar a mensagem
		System.out.println(String.format("--> Erro ao enviar \n%s", mensagem));
	}

	public static void printarMensagemRecebida(String mensagem) {
		printarDataHoraAtual();
		// Exibe no console a mensagem recebida
		System.out.println(String.format("<-- (recebido)\n%s", mensagem));
	}

	public static void printarComentario(String mensagem) {
		printarDataHoraAtual();
		// Exibe no console qualquer informação adicional
		System.out.println(String.format("/// %s", mensagem));
	}
	
	private static void printarDataHoraAtual() {
		printarDataAtual();
		printarHoraAtual();
	}

	private static void printarDataAtual() {
		// Retorna a data atual
		LocalDate dataAtual = LocalDate.now();
		// Define o formato da data
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/YYYY");
		// E a armazena numa variável
		String strDataAtual = formatter.format(dataAtual);

		// Exibe a data atual caso ainda não tenha sido exibida
		if (!strUltimaDataExibida.equals(strDataAtual)) {
			System.out.println("");
			System.out.print(strDataAtual);
			strUltimaDataExibida = strDataAtual;
			strUltimaHoraExibida = "";
		}
	}

	private static void printarHoraAtual() {
		// Retorna a hora atual
		LocalDateTime horaAtual = LocalDateTime.now();
		// Define o formato da hora
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		// E a armazena numa variável
		String strHoraAtual = formatter.format(horaAtual);

		// Exibe a hora atual caso ainda não tenha sido exibida na mesma data
		if (!strUltimaHoraExibida.equals(strHoraAtual)) {
			System.out.println("");
			System.out.println(strHoraAtual);
			System.out.println("");
			strUltimaHoraExibida = strHoraAtual;
		}
	}
	
	public static String retornarDataHoraAtual(String strPattern){
		// Retorna a data e hora atual
		LocalDateTime horaAtual = LocalDateTime.now();
		// Define o formato de acordo com o parâmetro
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(strPattern);
		// Retorna a data atual no formato solicitado
		return formatter.format(horaAtual);
	}
}
