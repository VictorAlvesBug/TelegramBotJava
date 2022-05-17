package telegram_bot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ComandosBasicos {
	public List<ComandoBot> retornarListaComandos(String comandoPai) {

		List<ComandoBot> listaComandos = new ArrayList<ComandoBot>();

		listaComandos
				.add(new ComandoBot("/nomedev", "Exibir o nome do desenvolvedor do bot", fNomeDev(), comandoPai));
		listaComandos.add(new ComandoBot("/dataatual", "Informa a �ltima data de atualiza��o do bot", fDataAtual(),
				comandoPai));
		listaComandos.add(new ComandoBot("/alfanumericos", "Grupo alfanumericos", fAlfanumericos(), comandoPai));
		listaComandos.add(new ComandoBot("/letras", "Grupo letras", fLetras(), "/alfanumericos"));
		listaComandos.add(
				new ComandoBot("/letrasmaiusculas", "Grupo letrasmaiusculas", fLetrasMaiusculas(), "/letras"));
		listaComandos.add(
				new ComandoBot("/letrasminusculas", "Grupo letrasminusculas", fLetrasMinusculas(), "/letras"));
		listaComandos.add(new ComandoBot("/numeros", "Grupo numeros", fNumeros(), "/alfanumericos"));
		listaComandos.add(new ComandoBot("/numerospares", "Grupo numerospares", fNumerosPares(), "/numeros"));
		listaComandos
				.add(new ComandoBot("/numerosimpares", "Grupo numerosimpares", fNumerosImpares(), "/numeros"));
		
		return listaComandos;
	}

	private static Function<String, String> fNomeDev() {
		// Retorna o nome do criador do chatbot
		return parametro -> String.format("Chatbot criado por Victor Alves Bugueno (Par�metro: '%s')", parametro);
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
			GerenciadorComandosBot.adicionarComandoNaPilha("/letrasmaiusculas");
			return "Voc� selecionou o grupo 'letrasmaiusculas'";
		};
	}

	private static Function<String, String> fLetrasMinusculas() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/letrasminusculas");
			return "Voc� selecionou o grupo 'letrasminusculas'";
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
			GerenciadorComandosBot.adicionarComandoNaPilha("/numerospares");
			return "Voc� selecionou o grupo 'numerospares'";
		};
	}

	private static Function<String, String> fNumerosImpares() {
		return parametro -> {
			GerenciadorComandosBot.adicionarComandoNaPilha("/numerosimpares");
			return "Voc� selecionou o grupo 'numerosimpares'";
		};
	}
}
