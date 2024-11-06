import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Tabuleiro extends JFrame {

    private JPanel painel;
    private JPanel menu;
    private JButton iniciarButton;
    private JButton resetButton;
    private JButton pauseButton;
    private JTextField placarField;
    private String direcao = "direita";
    private long tempoAtualizacao = 350; // Velocidade inicial mais baixa
    private int incremento = 10;
    private Quadrado maca;
    private List<Quadrado> cobra;
    private int larguraTabuleiro, alturaTabuleiro;
    private int placar = 0;
    private boolean jogoEmAndamento = false;
    private boolean pausado = false;

    public Tabuleiro() {

        larguraTabuleiro = alturaTabuleiro = 400;

        cobra = new ArrayList<>();
        cobra.add(new Quadrado(10, 10, Color.ORANGE)); // Cobra em laranja
        cobra.get(0).x = larguraTabuleiro / 2;
        cobra.get(0).y = alturaTabuleiro / 2;

        maca = new Quadrado(10, 10, Color.red);
        gerarNovaMaca();

        setTitle("Jogo da Cobrinha");
        setSize(alturaTabuleiro, larguraTabuleiro + 30);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        menu = new JPanel();
        menu.setLayout(new FlowLayout());

        iniciarButton = new JButton("Iniciar");
        resetButton = new JButton("Reiniciar");
        pauseButton = new JButton("Pausar");
        placarField = new JTextField("Placar: 0");
        placarField.setEditable(false);
        placarField.setPreferredSize(new Dimension(100, 30));

        menu.add(iniciarButton);
        menu.add(resetButton);
        menu.add(pauseButton);
        menu.add(placarField);

        painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Fundo quadriculado em cinza e preto
                setBackground(Color.BLACK);
                for (int y = 0; y < getHeight(); y += incremento) {
                    for (int x = 0; x < getWidth(); x += incremento) {
                        if ((x + y) / incremento % 2 == 0) {
                            g.setColor(Color.DARK_GRAY);
                        } else {
                            g.setColor(Color.BLACK);
                        }
                        g.fillRect(x, y, incremento, incremento);
                    }
                }

                // Desenhar a cobra
                for (Quadrado parte : cobra) {
                    g.setColor(parte.cor);
                    g.fillRect(parte.x, parte.y, parte.altura, parte.largura);
                }

                // Desenhar a maçã
                g.setColor(maca.cor);
                g.fillRect(maca.x, maca.y, maca.largura, maca.altura);
            }
        };

        add(menu, BorderLayout.NORTH);
        add(painel, BorderLayout.CENTER);

        setVisible(true);

        iniciarButton.addActionListener(e -> {
            Iniciar();
            painel.requestFocusInWindow();
        });

        resetButton.addActionListener(e -> Reiniciar());

        pauseButton.addActionListener(e -> Pausar());

        painel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A: // Esquerda
                        if (!direcao.equals("direita")) {
                            direcao = "esquerda";
                        }
                        break;
                    case KeyEvent.VK_D: // Direita
                        if (!direcao.equals("esquerda")) {
                            direcao = "direita";
                        }
                        break;
                    case KeyEvent.VK_W: // Cima
                        if (!direcao.equals("baixo")) {
                            direcao = "cima";
                        }
                        break;
                    case KeyEvent.VK_S: // Baixo
                        if (!direcao.equals("cima")) {
                            direcao = "baixo";
                        }
                        break;
                }
            }
        });

        painel.setFocusable(true);
        painel.requestFocusInWindow();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                larguraTabuleiro = painel.getWidth();
                alturaTabuleiro = painel.getHeight();
                ajustarPosicaoMaca();
                painel.repaint();
            }
        });
    }

    private void Iniciar() {
        jogoEmAndamento = true;
        pausado = false;

        new Thread(() -> {
            while (jogoEmAndamento) {
                if (!pausado) {
                    try {
                        Thread.sleep(tempoAtualizacao);

                        moverCobra();
                        checarColisao();
                        painel.repaint();
                        aumentarDificuldade();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void Reiniciar() {
        jogoEmAndamento = false;
        pausado = false;
        cobra.clear();
        cobra.add(new Quadrado(10, 10, Color.ORANGE)); // Cobra em laranja
        cobra.get(0).x = larguraTabuleiro / 2;
        cobra.get(0).y = alturaTabuleiro / 2;
        placar = 0;
        placarField.setText("Placar: 0");
        tempoAtualizacao = 300;
        gerarNovaMaca();
        painel.repaint();
        JOptionPane.showMessageDialog(this, "Jogo Reiniciado!", "Reset", JOptionPane.INFORMATION_MESSAGE);
    }

    private void Pausar() {
        pausado = !pausado;
        JOptionPane.showMessageDialog(this, pausado ? "Jogo Pausado!" : "Jogo Retomado!", "Pause", JOptionPane.INFORMATION_MESSAGE);
    }

    private void moverCobra() {
        for (int i = cobra.size() - 1; i > 0; i--) {
            cobra.get(i).x = cobra.get(i - 1).x;
            cobra.get(i).y = cobra.get(i - 1).y;
        }

        Quadrado cabeca = cobra.get(0);
        switch (direcao) {
            case "esquerda":
                cabeca.x -= incremento;
                break;
            case "direita":
                cabeca.x += incremento;
                break;
            case "cima":
                cabeca.y -= incremento;
                break;
            case "baixo":
                cabeca.y += incremento;
                break;
        }

        if (cabeca.x < 0) {
            cabeca.x = larguraTabuleiro - incremento;
        }
        if (cabeca.x >= larguraTabuleiro) {
            cabeca.x = 0;
        }
        if (cabeca.y < 0) {
            cabeca.y = alturaTabuleiro - incremento;
        }
        if (cabeca.y >= alturaTabuleiro) {
            cabeca.y = 0;
        }
    }

    private void checarColisao() {
        Quadrado cabeca = cobra.get(0);

        // Verifica se a cabeça da cobra toca a maçã
        if (Math.abs(cabeca.x - maca.x) < incremento && Math.abs(cabeca.y - maca.y) < incremento) {
            cobra.add(new Quadrado(10, 10, Color.ORANGE)); // Cresce a cobra
            placar++; // Aumenta o placar
            placarField.setText("Placar:" + placar); // Atualiza o placar na interface
            gerarNovaMaca();
        }

        // Colisão com o próprio corpo
        for (int i = 1; i < cobra.size(); i++) {
            if (cobra.get(i).x == cabeca.x && cobra.get(i).y == cabeca.y) {
                jogoEmAndamento = false;
                JOptionPane.showMessageDialog(this, "Fim de Jogo! Colisão com o próprio corpo.", "Fim", JOptionPane.INFORMATION_MESSAGE);
                Reiniciar();
            }
        }
    }

    private void gerarNovaMaca() {
        maca.x = (int) (Math.random() * (larguraTabuleiro / incremento)) * incremento;
        maca.y = (int) (Math.random() * (alturaTabuleiro / incremento)) * incremento;
    }

    private void ajustarPosicaoMaca() {
        if (maca.x >= larguraTabuleiro) {
            maca.x = larguraTabuleiro - incremento;
        }
        if (maca.y >= alturaTabuleiro) {
            maca.y = alturaTabuleiro - incremento;
        }
    }

    private void aumentarDificuldade() {
        if (tempoAtualizacao > 150) { // Reduz a velocidade mais lentamente
            tempoAtualizacao -= 1;
        }
    }

    public static void main(String[] args) {
        new Tabuleiro();
    }

    private class Quadrado {

        int x, y, largura, altura;
        Color cor;

        public Quadrado(int largura, int altura, Color cor) {
            this.largura = largura;
            this.altura = altura;
            this.cor = cor;
        }
    }
} 