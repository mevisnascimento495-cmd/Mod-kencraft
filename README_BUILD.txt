KenCraft Alpha 0.3.1
Minecraft 1.21.1
NeoForge 21.1.248
Java 21

Objetivo: sistema base de raça com persistência e captura de chat.

Fluxo:
1. Jogador entra sem raça.
2. Recebe mensagem para escolher Rinka ou Humano.
3. O chat é interceptado enquanto a raça é NONE.
4. A escolha é salva persistentemente.
5. Rinka/Humano recebe a explicação específica.
6. Após a escolha, o chat volta ao normal.

Arquivos principais:
- Kencraft.java
- Race.java
- PlayerData.java
- ModAttachments.java
- PlayerLoginHandler.java
- ChatSelectionHandler.java

Build:
gradle clean build

GitHub Actions:
O projeto usa Gradle 8.10.2 configurado pelo workflow do repositório.

JAR esperado:
build/libs/kencraft-0.5.0.jar
