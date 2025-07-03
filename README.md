# SafeShot Android

SafeShot é um aplicativo Android de código aberto focado em privacidade, que permite remover ("queimar") metadados sensíveis de fotos antes de compartilhar ou armazenar. Ideal para quem deseja proteger sua localização, dispositivo e outros dados pessoais embutidos em imagens.

## Funcionalidades

- **Remoção de metadados EXIF** de fotos tiradas pelo app ou recebidas via compartilhamento.
- **Captura de fotos** diretamente pela câmera do app.
- **Seleção de fotos da galeria** para limpeza manual.
- **Recebimento de fotos via "Compartilhar para SafeShot"**: limpe metadados de qualquer app (galeria, WhatsApp, câmera, etc).
- **Visualização dos metadados originais e limpos** antes e depois da limpeza.
- **Download e compartilhamento da imagem limpa** diretamente do app.
- **Log de limpeza**: histórico das fotos processadas, com data/hora e status.
- **Notificações** ao remover metadados automaticamente (quando possível).
- **Avisos claros sobre privacidade e limitações do Android**.
- **Interface simples, rápida e intuitiva**.

## Como usar

1. **Tirar foto pelo app**: Use o botão "Tirar Foto" para capturar e limpar metadados.
2. **Selecionar da galeria**: Escolha uma foto existente e limpe os metadados.
3. **Compartilhar para SafeShot**: Na galeria ou outro app, selecione uma foto, clique em "Compartilhar" e escolha o SafeShot. O app irá limpar os metadados automaticamente.
4. **Ver log de limpeza**: Acesse o menu no topo do app para ver o histórico de fotos processadas.

## Privacidade e Limitações

- **Todo o processamento é local**: Nenhuma foto ou dado é enviado para servidores.
- **Android 10+**: Por limitações do sistema, não é possível automatizar a limpeza de fotos tiradas por outros apps. Use o fluxo de compartilhamento para máxima privacidade.
- **Permissões**: O app solicita apenas as permissões necessárias para funcionamento (câmera, armazenamento).

## Build e Instalação

1. Abra a pasta `safeshot_android` no Android Studio.
2. Sincronize o Gradle.
3. Vá em `Build > Build APK(s)` para gerar o APK.
4. Instale o APK em seu aparelho Android.

## Contribuição

Pull requests são bem-vindos! Sinta-se à vontade para sugerir melhorias, reportar bugs ou contribuir com novas funcionalidades.

## Licença

Este projeto é open source sob a licença MIT. 