package telegram_bot;

import java.util.ArrayList;
import java.util.List;

// Classe respons�vel por gerenciar o jogo da velha, assim como os status do jogo, o placar e o vencedor
public class TicTacToeGame {
	private List<String> listaPosicoes;

	private String jogadorAtual;

	private int qtdePontosX;
	private int qtdePontosO;

	public List<ComandoBot> retornarListaComandos(String comandoPai, GerenciadorComandosBot gerenciadorComandos) {

		// Inicializa os pontos de cada jogador como zero
		qtdePontosX = 0;
		qtdePontosO = 0;

		// Inicializa a lista do jogo (todos ficar�o no contexto comandoPai)
		List<ComandoBot> listaComandos = new ArrayList<ComandoBot>();

		// Comando principal do jogo (para iniciar)
		ComandoBot comandoBot = new ComandoBot("/ticTacToe", "Jogo da Velha", a -> {

			// Inicializando as posi��es do jogo da velha (de 1 at� 9)
			listaPosicoes = new ArrayList<String>();

			// Preenchendo lista de posi��es
			for (int i = 0; i < 9; i++) {
				listaPosicoes.add("");
			}

			// Define o jogador "O" como sendo o jogador inicial
			jogadorAtual = "O";

			// Comandos para selecionar as posi��es do jogo da velha (tic-tac-toe).
			for (int i = 0; i < 9; i++) {
				gerenciadorComandos
						.adicionarComando(new ComandoBot("/0" + (i + 1), "Op��o de jogada", fOpcao(i), "/ticTacToe"));
			}

			// Muda para o contexto do jogo
			gerenciadorComandos.adicionarComandoNaPilha("/ticTacToe");

			// Atualiza o status do jogo
			atualizarJogo();

			return "";
		}, comandoPai);

		// Adicionado comando principal do jogo
		listaComandos.add(comandoBot);

		return listaComandos;
	}

	public IFuncao fOpcao(int indice) {
		return a -> {
			GerenciadorComandosBot gerenciadorComandos = new GerenciadorComandosBot();

			// Marca a posi��o selecionada pelo jogador
			listaPosicoes.set(indice, jogadorAtual);

			// Alterna para o outro jogador
			alternarJogador();

			// Atualiza o status do jogo
			atualizarJogo();

			// Busca por um vencedor
			String vencedor = retornarVencedor();

			// Caso esteja null, ainda n�o temos um vencedor
			// Caso esteja "" (vazio), o jogo deu empate
			// Caso contr�rio, temos realmente um vencedor
			if (vencedor == null) {
				gerenciadorComandos.removerComando("/0" + (indice + 1));
				return gerenciadorComandos.getHelp(true);
			} else if (vencedor.equals("")) {
				gerenciadorComandos.voltarUmComandoNaPilha();
				gerenciadorComandos.tentarExecutarFuncao("/ticTacToe", null);
				return "Empate!!!\n\n" + gerenciadorComandos.getHelp(true);
			}

			// Adiciona os pontos para o vencedor
			switch (vencedor) {
			case "X":
				qtdePontosX++;
				break;

			case "O":
				qtdePontosO++;
				break;
			}

			// Caso tenha vencido 3 vezes, zera os pontos e exibe o vencedor master
			if (qtdePontosX == 3 || qtdePontosO == 3) {
				qtdePontosX = 0;
				qtdePontosO = 0;
				gerenciadorComandos.voltarUmComandoNaPilha();
				return "Parab�ns, jogador \"" + vencedor + "\", voc� venceu!!!\n\n" + gerenciadorComandos.getHelp(true);
			}

			// Caso contr�rio, exibe o vencedor atual e zera o jogo
			gerenciadorComandos.voltarUmComandoNaPilha();
			gerenciadorComandos.tentarExecutarFuncao("/ticTacToe", null);
			return "Jogador \"" + vencedor + "\" venceu o Jogo da Velha!!!\n\n" + gerenciadorComandos.getHelp(true);

		};
	}

	public void atualizarJogo() {
		GerenciadorComandosBot gerenciadorComandos = new GerenciadorComandosBot();

		String emojiFeliz = "   =D";
		String emojiTriste = "   =(";
		String emojiJogadorX = "";
		String emojiJogadorO = "";

		// Verifica se algum jogador est� nafrente, caso esteja, exibe os emojis
		if (qtdePontosX > qtdePontosO) {
			emojiJogadorX = emojiFeliz;
			emojiJogadorO = emojiTriste;
		} else if (qtdePontosX < qtdePontosO) {
			emojiJogadorO = emojiFeliz;
			emojiJogadorX = emojiTriste;
		}

		// Constr�i o tabuleiro do jogo (inicialmente com todas as 9 posi��es
		// dispon�veis)
		StringBuilder sbConteudo = new StringBuilder();
		sbConteudo.append("\n\n>>>>>>>>>> Jogo da Velha - Tic Tac Toe <<<<<<<<<<\n");
		sbConteudo.append("\nPlacar (at� 3 pontos):");
		sbConteudo.append("\nX --> " + qtdePontosX + emojiJogadorX);
		sbConteudo.append("\nO --> " + qtdePontosO + emojiJogadorO);
		sbConteudo.append("\n\nEst� na vez do jogador \"" + jogadorAtual + "\"");
		sbConteudo.append("\nEscolha uma posi��o para jogar...\n");
		sbConteudo.append("\n      /01  |  /02  |  /03  ");
		sbConteudo.append("\n    -------------------------");
		sbConteudo.append("\n      /04  |  /05  |  /06  ");
		sbConteudo.append("\n    -------------------------");
		sbConteudo.append("\n      /07  |  /08  |  /09  ");

		String conteudo = sbConteudo.toString();

		// Substitui as posi��es j� ocupadas pelo jogador que a ocupa
		for (int i = 0; i < 9; i++) {
			String jogadorPosicaoAtual = listaPosicoes.get(i);
			if (jogadorPosicaoAtual != null && !jogadorPosicaoAtual.isEmpty()) {
				conteudo = conteudo.replaceAll("/0" + (i + 1), " " + jogadorPosicaoAtual + " ");
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

		// Conjunto de posi��es que configuram uma vit�ria
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

		// Itera cada um dos conjunto de posi��es que configuram uma vit�ria e verifica
		// se algum est� preenchido por um mesmo jogador
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

		// Caso tenha um vencedor, retorna o vencedor
		if (vencedor != null) {
			return vencedor;
		}

		// Caso n�o tenha um vencedor e n�o tenha nenhuma posi��o dispon�vel, retorna
		// vazio, informando que ocorreu um empate
		if (listaPosicoes.indexOf("") == -1) {
			return "";
		}

		// Caso retorne nulo, significa que o jogo ainda n�o terminou
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
