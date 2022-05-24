package telegram_bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.intellij.lang.annotations.RegExp;

import telegram_bot.Models.FilmeModel;

public class TicTacToeGame {
	private List<String> listaPosicoes;

	private String jogadorAtual;

	private int qtdePontosX;
	private int qtdePontosO;

	public List<ComandoBot> retornarListaComandos(String comandoPai, GerenciadorComandosBot gerenciadorComandos) {

		qtdePontosX = 0;
		qtdePontosO = 0;

		// Inicializa a lista do jogo (todos ficarão no contexto comandoPai)
		List<ComandoBot> listaComandos = new ArrayList<ComandoBot>();

		// Comando principal do jogo (para iniciar)
		ComandoBot comandoBot = new ComandoBot("/ticTacToe", "Jogo da Velha", a -> {

			// Inicializando as posições do jogo da velha (de 1 até 9)
			listaPosicoes = new ArrayList<String>();

			// Preenchendo lista de posições
			for (int i = 0; i < 9; i++) {
				listaPosicoes.add("");
			}

			jogadorAtual = "O";

			// Comandos para selecionar as posições do jogo da velha (tic-tac-toe).
			for (int i = 0; i < 9; i++) {
				gerenciadorComandos
						.adicionarComando(new ComandoBot("/0" + (i + 1), "Opção de jogada", fOpcao(i), "/ticTacToe"));
			}

			// Muda para o contexto do jogo
			gerenciadorComandos.adicionarComandoNaPilha("/ticTacToe");

			atualizarJogo();

			return "Bem-vindo ao Jogo da Velha";
		}, comandoPai);

		// Adicionado comando principal do jogo
		listaComandos.add(comandoBot);

		return listaComandos;
	}

	public IFuncao fOpcao(int indice) {
		return a -> {
			GerenciadorComandosBot gerenciadorComandos = new GerenciadorComandosBot();

			listaPosicoes.set(indice, jogadorAtual);
			alternarJogador();
			atualizarJogo();

			String vencedor = retornarVencedor();

			if (vencedor == null) {
				gerenciadorComandos.removerComando("/0" + (indice + 1));
				return gerenciadorComandos.getHelp(true);
			} else if (vencedor.equals("")) {
				gerenciadorComandos.voltarUmComandoNaPilha();
				gerenciadorComandos.tentarExecutarFuncao("/ticTacToe", null);
				return "Empate!!!\n\n" + gerenciadorComandos.getHelp(true);
			}

			switch (vencedor) {
			case "X":
				qtdePontosX++;
				break;

			case "O":
				qtdePontosO++;
				break;
			}

			if (qtdePontosX == 3 || qtdePontosO == 3) {
				qtdePontosX = 0;
				qtdePontosO = 0;
				gerenciadorComandos.voltarUmComandoNaPilha();
				return "Parabéns, jogador \"" + vencedor + "\", você venceu!!!\n\n"
						+ gerenciadorComandos.getHelp(true);
			} else {
				gerenciadorComandos.voltarUmComandoNaPilha();
				gerenciadorComandos.tentarExecutarFuncao("/ticTacToe", null);
				return "Jogador \"" + vencedor + "\" venceu o Jogo da Velha!!!\n\n" + gerenciadorComandos.getHelp(true);
			}
		};
	}

	public void atualizarJogo() {
		GerenciadorComandosBot gerenciadorComandos = new GerenciadorComandosBot();
		StringBuilder sbConteudo = new StringBuilder();

		String emojiFeliz = "   =D";
		String emojiTriste = "   =(";
		String emojiJogadorX = "";
		String emojiJogadorO = "";

		if (qtdePontosX > qtdePontosO) {
			emojiJogadorX = emojiFeliz;
			emojiJogadorO = emojiTriste;
		} else if (qtdePontosX < qtdePontosO) {
			emojiJogadorO = emojiFeliz;
			emojiJogadorX = emojiTriste;
		}

		sbConteudo.append("\n\n>>>>>>>>>> Jogo da Velha - Tic Tac Toe <<<<<<<<<<\n");
		sbConteudo.append("\nPlacar (até 3 pontos):");
		sbConteudo.append("\nX --> " + qtdePontosX + emojiJogadorX);
		sbConteudo.append("\nO --> " + qtdePontosO + emojiJogadorO);
		sbConteudo.append("\n\nEstá na vez do jogador \"" + jogadorAtual + "\"");
		sbConteudo.append("\nEscolha uma posição para jogar...\n");
		sbConteudo.append("\n      /01  |  /02  |  /03  ");
		sbConteudo.append("\n    -------------------------");
		sbConteudo.append("\n      /04  |  /05  |  /06  ");
		sbConteudo.append("\n    -------------------------");
		sbConteudo.append("\n      /07  |  /08  |  /09  ");

		String conteudo = sbConteudo.toString();

		for (int i = 0; i < 9; i++) {
			String posicaoAtual = listaPosicoes.get(i);
			if (posicaoAtual != null && !posicaoAtual.isEmpty()) {
				conteudo = conteudo.replaceAll("/0" + (i + 1), " " + posicaoAtual + " ");
			}
		}

		gerenciadorComandos.retornarComandoBotPai().setConteudoPersonalizado(conteudo);
	}

	public String retornarVencedor() {

		String vencedor = null;

		// null --> jogo em andamento
		// "" --> Empate
		// "O" --> "O" venceu
		// "X" --> "X" venceu

		// Posições que configuram uma vitória
		List<String> listaTrioLinha = new ArrayList<String>();

		// Linhas horizontais
		listaTrioLinha.add("012");
		listaTrioLinha.add("345");
		listaTrioLinha.add("678");

		// Linhas verticais
		listaTrioLinha.add("036");
		listaTrioLinha.add("147");
		listaTrioLinha.add("258");

		// Linhas Diagonais
		listaTrioLinha.add("048");
		listaTrioLinha.add("246");

		// Itera cada uma das posições que configuram uma vitória e verifica se alguma
		// esta preenchida com o mesmo jogador
		for (String trioLinha : listaTrioLinha) {
			int posicao1 = Integer.parseInt(trioLinha.substring(0, 1));
			int posicao2 = Integer.parseInt(trioLinha.substring(1, 2));
			int posicao3 = Integer.parseInt(trioLinha.substring(2, 3));

			String caracter1 = listaPosicoes.get(posicao1);
			String caracter2 = listaPosicoes.get(posicao2);
			String caracter3 = listaPosicoes.get(posicao3);

			if (caracter1.equals(caracter2) && caracter1.equals(caracter3)) {
				if (caracter1.equals("O")) {
					vencedor = "O";
				} else if (caracter1.equals("X")) {
					vencedor = "X";
				}
			}
		}

		if (vencedor != null) {
			return vencedor;
		}

		if (listaPosicoes.indexOf("") == -1) {
			return "";
		}

		return null;
	}

	public void alternarJogador() {
		if (jogadorAtual.equals("X")) {
			jogadorAtual = "O";
		} else {
			jogadorAtual = "X";
		}
	}
}
