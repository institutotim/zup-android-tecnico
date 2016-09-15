# Changelog

# 1.2.8
### Modificado
- Serialização de imagens em base64 somente no momento de sincronização a fim de evitar estouro de memória

# 1.2.7
### Adicionado
- Tela de relatos relacionados em caso

### Modificado
- Lib de anexar fotos para uma do gradle

# 1.2.6
### Resolvido
- Problema de sincronização de relatos e inventário em ZUP Unicef

# 1.2.5

### Resolvido
- Ajustadas nomenclaturas do ZUP Unicef
- Logo do Instituto TIM estava distorcida

# 1.2.4

### Resolvido
- Limpando token quando a token for expirada para que ela não seja enviada quando o usuário logar novamente

# 1.2.3

### Resolvido
- Erros reportados no crashlytics

# 1.2.2

### Resolvido
- Erro de IndexOutOfBoundsException no Autocomplete

### Modificado
- Logo do Instituto TIM

### Adicionado
- Suporte a filtros e ordenação na busca de relatos por palavra-chave

# 1.2.1

### Adicionado
- Opção de criar item de inventário dentro do caso
- Opçao de abrir item de inventário na tela de selecionar item em caso

## 1.2.0

### Adicionado
- Seção de casos relacionados na tela de relato
- Seção de casos relacionados na tela de inventário
- Suporte à criação de caso relacionado à mudança de status de relato.
- Suporte a links na seção de comentários em um relato (Resposta ao solicitante)

### Modificado
- Substituídas as palavras de acordo com a Unicef
- Substituídos Toasts por Snackbars

### Melhoria
- Criado um tipo de erro para cada status, a fim de melhorar o log no crashlytics (em vez de RetrofitError, agora é NotFoundError, pro ex)

### Modificado
- Possibilitada a busca por palavra-chave com filtros.
- Adicionado o número do logradouro na lista de relatos
- Melhorada a performance da busca de relato.
- Adicionadas opções de ordenação de relatos

### Resolvido
- Crash ao adicionar foto em item de inventário

## 1.1.18
### Resolvido
- Criação de relato dava crash
- Busca com acentuação ficava carregando indefinidamente

## 1.1.17
### Resolvido
- Edição de relato não funcionava o envio de novas imagens nem a mudança de endereço

## 1.1.16
### Modificado
- Casos relacionados a relatos sendo mostrados na dela de relatos (unicef)
- Permitido o envio de mais imagens
- Melhorado o layout do botão de remover imagem de inventário
- Grupo solucionador era modificado sem modificar o endereço
- Resolvidos crashes do fabric.io

## 1.1.15
### Modificado
- Serialização e desserialização de itens de inventário otimizada (casos de muitos dados pode dar erro)
- Mudança de BD da parte de itens de inventário, categorias de inventário e itens de sincronização
- Otimização no carregamento de listas infinitas de inventário e relato
- Uso do Picasso para o carregamento de todas as imagens na parte de inventário

## 1.1.14
### Resolvido
- Localização automática não vinha no mapa de localização
- Problemas ao receber as coordenadas do GPS
- Permitindo a visualização e remoção de imagens ja enviadas ao editar item de inventário

## 1.1.13

### Resolvido
- Apagando itens de sincronização apenas quando solicitado.
- Crash ao criar item de inventário offline
- Após clicar em sincronizar agora e voltar, volta para a tela de carregando
- Localização GPS não está funcionando

### Adicionado
- Número da versão nas telas de login e de carregando dados

## 1.1.12

### Resolvido
- Validação do status foi feita

## 1.1.11

### Resolvido
- Itens de inventário salvos offline não estavam sendo mostrados
- NullPointerException ao clicar em Localizar Mapa
- Renomeado editar localização para editar item (tela de sincronização)
- Ordenada as seçoes na maneira correta
- Validando os dados do item de inventário sem passar adiante

### Modificado
- Layout de criação/edição de item de inventário (Fragment, maior controle dos dados)
- Enviando dados de request ao Crashlytics em erros de sincronização

## 1.1.10

### Tratado
- Atualização de estrutura de DB da parte de inventário

## 1.1.9

### Removido
- Carregando fluxos e casos

## 1.1.8

### Resolvido
- Cache de campos de inventário não permitiam a atualização
- Inventário > Opção de campo de múltipla escolha desabilitado está sendo mostrado
- Inventário > Ao editar um item offline, o mesmo não é mostrado após a conclusão da edição.
- Inventário > Ao editar um item de inventário offline e o mesmo já ter sido deletado no painel, é preciso perguntar se o usuário gostaria de criar um item igual
- Tratamento de erros ao sincronizar relatos. A partir de agora o usuário poderá visualizar exatamente o erro retornado pela API.

### Modificado
- Novo layout do formulário de inventário
- Casos e fluxos parcialmente funcionando
- Token sendo enviado via Header e não via Param
- Novo layout de busca e filtro de inventário igual aos relatos
- Ordenação das seções de categoria de inventário, ao cadastrar um novo item as seções e os campos estão na ordem que aparecem no painel
- Implementado alerta quando o usuário tentar fazer logout com itens pendentes de sincronização. A partir de agora o usuário só conseguirá fazer o logout quando sincronizar todos os relatos e inventário ou cancelar a sincronização dos itens.

## 1.1.7

### Resolvido
- Tratamento de erros ao sincronizar inventário. A partir de agora o usuário poderá visualizar exatamente o erro retornado pela API.

## 1.1.6

### Modificado
- Novo layout para alteração do grupo e usuário responsável dentro do relato
- Retirada a obrigatoriedade da seção de inventário
- Resolvidos crashes do app no zup boa vista
- Otimizado código de criação de item de inventário

## 1.1.5

### Adicionado
- Tela de histórico de casos
- Aviso de que tem sincronização pendente
- Telas de login com imagens da versão de Floripa, SBC e Boa Vista

### Resolvido
- Campo CEP não está sendo enviado na criação/edição do relato
- Enviando campo número separado do endereço ao criar ou editar relato
- Bug ao buscar pore relatos por protocolo ou endereço
- Checkbox de selecionar usuários não funciona
- Ao rotacionar o aparelho, a imagem adicionada a posteriori é perdida
- Impedindo que o usuário edite um relato que está pendente de sincronização


## 1.1.4

### Refatorado
- Detalhes de inventário e criação de inventário

### Resolvido
- Erro com o mapa de inventário
- Erro ao criar e editar relatos offline
- O campo de cidade estava mostrando informações do bairro
- Envio de dados de inventário para o servidor
- Imagens alinhadas a esquerda na tela de criação de relato
- Todos os status sendo mostrados na lista de status (filtro de relatos)
- Atualizando histórico após envio de comentário interno ou resposta ao solicitante

### Adicionado
- botão de limpar filtros no menu de contexto da lista de relatos
- Tratamento de permissões no loading inicial (evitando forbidden de dados que o usuário não possa ter acesso pela API)

## 1.1.3

### Adicionado
- Posibilidade de alterar status e  grupo responsável

### Modificado
- Em vez de respostas ao munícipe, ser respostas ao solicitante

### Resolvido
- Tela branca sem relatos após pesquisa vazia
- Imagem era removida ao cancelar ação de adicionar imagem

## 1.1.2

### Resolvido
- Selecionar categoria em filtro de relatos dava crash
- Filtro por data desalinhado
- Problemas em campos de busca

## 1.1.1

### Adicionado
- Carregamento de fluxos no loading inicial do app

## 1.1.0

### Adicionado
- Funcionalidade de adicionar legenda das imagens de relato

### Resolvido
- Permissionamento de status
- Manter dados ao mudar a orientação do dispositivo

## 1.0.4

### Adicionado
- Funcionalidades da actionbar da tela de lista de casos (refresh na action bar)
- Restrições de permissão de usuário para relatos
- Tela de recuperação de senha

### Resolvido
- Seção "Notificação Emitida" estava com informações da notificação errada
- Menu de contexto com o nome da label errado
- Botão back saindo do app
- Atualizando lista ao criar um novo relato
- Trocada a fraseologia ao cancelar ação de sincronização
- Thumbnail da imagem do relato está sendo exibido na visualização do relato

### Modificado
- Layout de login

## 1.0.3

### Adicionado
- Imagem de sync ao lado de sincronizar na sidebar
- Botão de refresh na actionbar de relatos

### Refatorado
- Listagem e mapas de inventário
- Listagem de casos
- Tela de sidebar
- Classes de casos e fluxos

### Resolvido
- Funcionalidade de rotacionar e cortar imagem ao subir a um relato
- Bugs de atualização de relato offline quando o relato já foi excluido no servidor

### Modificado
- Limpar Filtros em seleção de categorias e usuários aparece sempre ao usuário, mesmo que não haja nenhum ítem selecionado.
- Seção de notificações emitidas no relato mostra agora todas as notificações emitidas
- Seção de notificações mostra o status de cada notificação
- Layout de sidebar

## 1.0.2

### Resolvido
- Relatos a serem deletados também são deletados localmente
- Ocultada a seção de notificações quando não há notificações no relato
- Botao de voltar não estava saindo do app
- Relato criado agora aparece na lista de relatos

### Implementado
- Validação do CPF ao cadastrar usuário
- Fraseologias de notificações no histórico

## 1.0.1

### Resolvido
- Nome do usuário estava sobrepondo o botão de sair na tela de perfil do usuário
- Erro de que o relato local deletado, quando sincronizado, não era deletado localmente.
- Problema de que as imagens não eram carregadas no menu de edição de relato quando o mesmo estava localmente e sem conexão.
- Barras de progresso padronizadas no app

## 1.0.0
- Versão estável inicial