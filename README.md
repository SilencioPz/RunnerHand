RunnerHand - Speedrun Timer 🎮⏱️

RunnerHand é um timer de speedrun amador desenvolvido em Java, projetado para controle preciso sobre splits com recursos avançados de personalização.
--------------------------------------------------------------------------------------
✨ Recursos Principais
--------------------------------------------------------------------------------------
🎯 Controle de Splits
--------------------------------------------------------------------------------------
    Timer com precisão de centésimos de segundo

    Splits personalizáveis com nomes e imagens

    Sistema de comparação com melhores tempos

    Pular splits, voltar split anterior

    Comparação em tempo real com runs anteriores

🎨 Interface

    Interface dark mode

    Ícones para cada split (suporte a JPG, PNG)

    Design responsivo e adaptável

    Sistema de cores para indicar desempenho
--------------------------------------------------------------------------------------
⌨️ Atalhos de Teclado Completos

    Iniciar/Pausar: Numpad +

    Split: Numpad *

    Reset: Numpad 0

    Split Anterior: Numpad 4

    Pular Split: Numpad 6

    Comparar Anterior: Numpad 1

    Finalizar Run: Numpad 8

    Todas as teclas são personalizáveis
--------------------------------------------------------------------------------------
💾 Sistema de Salvamento

    Salvamento em HTML: Com imagens embeddadas (abre em qualquer navegador)

    Salvamento em TXT: Formato simples para análise

    Carregamento de HTML: Recupera runs salvas com imagens

    Contador de tentativas: Por título de run

    Configurações persistentes: Teclas e splits salvos automaticamente
--------------------------------------------------------------------------------------
🚀 Como Usar
--------------------------------------------------------------------------------------
Pré-requisitos

    Java 8 ou superior

    Sistema operacional com suporte a Java Swing
--------------------------------------------------------------------------------------
Execução

    Clone o repositório:
    bash

git clone https://github.com/seu-usuario/RunnerHand.git
cd RunnerHand

Compile o projeto:
bash

javac -d bin **/*.java
--------------------------------------------------------------------------------------
Execute:

java -cp bin Main

    Ou execute diretamente o arquivo RunnerHand.jar (disponível na aba Releases).
--------------------------------------------------------------------------------------
Configuração Inicial

    Clique em "Config" para adicionar seus splits

    Nomeie cada split e opcionalmente adicione uma imagem

    Clique em "Salvar Configuração"

    Use os atalhos de teclado ou botões para controlar o timer
--------------------------------------------------------------------------------------
📁 Estrutura do Projeto
--------------------------------------------------------------------------------------

RunnerHand/
├── src/

│   ├── Main.java                    # Ponto de entrada

│   ├── RunnerHandGUI.java           # Interface gráfica principal

│   ├── MyTimer.java                 # Lógica do timer

│   ├── Run.java                     # Gerenciamento da run

│   ├── Split.java                   # Representação de split

│   ├── KeyConfig.java               # Configuração de teclas

│   ├── RunCounter.java              # Contador de tentativas

│   ├── ImageLoader.java             # Carregador de imagens

│   └── resources/                   # Assets do projeto

│       ├── silenciopz_logo2.png     # Logo do desenvolvedor

│       ├── silenciopz_logo2icon.png # Ícone do aplicativo

├── runnerhand_config.properties     # Configurações salvas

├── run_counter.properties           # Contadores salvos

└── .gitignore                       # Arquivos ignorados pelo Git
--------------------------------------------------------------------------------------
🔧 Personalização
--------------------------------------------------------------------------------------
Teclas de Atalho

    Clique em "Teclas"

    Clique no campo desejado

    Pressione a nova tecla

    Configurações são salvas automaticamente
--------------------------------------------------------------------------------------
Splits com Imagens

    Ao adicionar split, clique em "Selecionar"

    Escolha uma imagem (JPG, PNG, GIF)

    A imagem aparecerá na tabela de splits
--------------------------------------------------------------------------------------
💡 Dicas de Uso
--------------------------------------------------------------------------------------
Para Speedrunners

    Use Numpad 1 para alternar entre mostrar/ocultar comparações

    Numpad 7 para alternar comparação com run anterior

    Salve sempre em HTML para ter um backup completo com imagens

    O contador de tentativas ajuda a acompanhar progresso
--------------------------------------------------------------------------------------
Para Streamers

    Splits com imagens ficam visuais no stream

    Salve runs em HTML para compartilhar com viewers

    Sistema de cores ajuda espectadores a entenderem o desempenho
--------------------------------------------------------------------------------------
📊 Exportação de Dados
--------------------------------------------------------------------------------------
Formato HTML

    Inclui todas as imagens dos splits (EMBEDDADAS em base64)

    Pode ser aberto em qualquer navegador

    Compatível com LibreOffice Writer e Microsoft Word

    Layout profissional com cores do tema
--------------------------------------------------------------------------------------
Formato TXT

    Formato simples para análise em planilhas

    Inclui tempos parciais e totais

    Lista de melhores tempos por split
--------------------------------------------------------------------------------------
🛠️ Desenvolvimento
Compilando o JAR
bash

# Dentro da pasta do projeto:
javac -d bin **/*.java
jar cfe RunnerHand.jar Main -C bin .
--------------------------------------------------------------------------------------
Adicionando Novos Recursos

O projeto é modular e fácil de estender:

    Novas funcionalidades no timer: MyTimer.java

    Novos elementos na interface: RunnerHandGUI.java

    Novos formatos de exportação: Run.java
--------------------------------------------------------------------------------------
🐛 Solução de Problemas

Entre em contato por e-mail, acessando meu Linkedin: https://www.linkedin.com/in/bruno-c-perez-a712b0276/

--------------------------------------------------------------------------------------
Teclas não Funcionam

    Verifique se o NumLock está ativado

    Confirme as configurações em runnerhand_config.properties

    Reinicie o aplicativo após alterar teclas
--------------------------------------------------------------------------------------
Erro ao Salvar HTML

    Verifique permissões de escrita na pasta

    Certifique-se de ter espaço em disco

    Imagens muito grandes podem causar problemas
--------------------------------------------------------------------------------------
🤝 Contribuindo

    Faça um Fork do projeto

    Crie uma branch para sua feature (git checkout -b feature/AmazingFeature)

    Commit suas mudanças (git commit -m 'Add some AmazingFeature')

    Push para a branch (git push origin feature/AmazingFeature)

    Abra um Pull Request
--------------------------------------------------------------------------------------
📄 Licença

Este projeto está sob a licença GNU GPL v3.0. Veja o arquivo LICENSE para mais detalhes.
--------------------------------------------------------------------------------------
👨‍💻 Desenvolvedor: SilencioPz

Site: https://silenciopz.neocities.org/
