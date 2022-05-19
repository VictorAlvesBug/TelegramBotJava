package telegram_bot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ComandosBasicos {
	public List<ComandoBot> retornarListaComandos(String comandoPai) {

		List<ComandoBot> listaComandos = new ArrayList<ComandoBot>();

		listaComandos
				.add(new ComandoBot("/nomeDev", "Exibir o nome do desenvolvedor do bot", fNomeDev(), comandoPai));
		listaComandos.add(new ComandoBot("/dataAtual", "Informa a �ltima data de atualiza��o do bot", fDataAtual(),
				comandoPai));
		listaComandos.add(new ComandoBot("/alfanumericos", "Grupo alfanumericos", fAlfanumericos(), comandoPai));
		listaComandos.add(new ComandoBot("/letras", "Grupo letras", fLetras(), "/alfanumericos"));
		listaComandos.add(
				new ComandoBot("/letrasMaiusculas", "Grupo letrasmaiusculas", fLetrasMaiusculas(), "/letras"));
		listaComandos.add(
				new ComandoBot("/letrasMinusculas", "Grupo letrasminusculas", fLetrasMinusculas(), "/letras"));
		listaComandos.add(new ComandoBot("/numeros", "Grupo numeros", fNumeros(), "/alfanumericos"));
		listaComandos.add(new ComandoBot("/numerosPares", "Grupo numerospares", fNumerosPares(), "/numeros"));
		listaComandos
				.add(new ComandoBot("/numerosImpares", "Grupo numerosimpares", fNumerosImpares(), "/numeros"));
		
		return listaComandos;
	}

	private static Function<String, String> fNomeDev() {
		// Retorna o nome do criador do chatbot
		return parametro -> "Chatbot criado por Victor Alves Bugueno";
	}

	private static Function<String, String> fDataAtual() {
		// Retorna o hor�rio atual
		return parametro -> "Agora s�o: " + Console.retornarDataHoraAtual("HH:mm (dd/MM/yyyy)");
	}

	private static Function<String, String> fAlfanumericos() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/alfanumericos");
			return "Voc� selecionou o grupo 'alfanumericos'";
		};
	}

	private static Function<String, String> fLetras() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/letras");
			return "Voc� selecionou o grupo 'letras'";
		};
	}

	private static Function<String, String> fLetrasMaiusculas() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/letrasMaiusculas");
			return "Voc� selecionou o grupo 'letrasMaiusculas'";
		};
	}

	private static Function<String, String> fLetrasMinusculas() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/letrasMinusculas");
			return "Voc� selecionou o grupo 'letrasMinusculas'";
		};
	}

	private static Function<String, String> fNumeros() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/numeros");
			return "Voc� selecionou o grupo 'numeros'";
		};
	}

	private static Function<String, String> fNumerosPares() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/numerosPares");
			return "Voc� selecionou o grupo 'numerosPares'";
		};
	}

	private static Function<String, String> fNumerosImpares() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/numerosImpares");
			return "Voc� selecionou o grupo 'numerosImpares'";
		};
	}
}
