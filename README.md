# Hidrômetro Inteligente
Sistema para monitoramento de consumo de água de cidades inteligentes.

## Descrição da solução

O objetivo deste protótipo é
que os dados de consumo de água sejam gerados a partir de um dispositivo hidrômetro inteligente, com
capacidade de rede, instalado nas residências dos consumidores. 

Os dados de consumo gerados pelas
residências devem ser agregados remotamente em uma nuvem da concessionária, ficando também disponíveis
on-line para os clientes através da Internet.

## Funcionalidades

Os dados serão agregados visando monitorar o
abastecimento de água, medir o consumo de cada cliente, gerar a fatura a ser paga, bem como alertar sobre
um possível vazamento de água em determinada zona da cidade. O administrador poderá também cortar o
fornecimento de água da residência caso o usuário possua alguma conta em aberto. Caso o usuário quite o
débito, o sistema deve liberar o fornecimento de água imediatamente.

Também, os usuários do serviço podem acessar o sistema de forma online para acompanhar o
consumo da água, com datas/horários específicos do consumo e o total acumulado. Dessa forma, os usuários
podem aprender a regular seus hábitos para um maior controle de gastos, evitando surpresas ao final do mês.

## Requisitos

- O produto deve ser desenvolvido e testado através de contêineres Docker;
- As interfaces devem ser projetadas e implementadas através de protocolo baseado em uma API REST;
- Para facilitar a avaliação do protótipo, o hidrômetro será simulado através de um software para geração de dados fictícios sobre o consumo de água. Para uma emulação realista do cenário proposto, cada dispositivo hidrômetro deve ser executado em um container Docker separado em um computador no laboratório;
- O hidrômetro deve possuir uma interface para controlar a geração dos dados em tempo real. Por exemplo, através da interface deve ser possível definir, aumentar ou diminuir a vazão da água em m3/s;
- NENHUM framework de terceiro deve ser usado para implementar a solução do problema. Apenas os mecanismos básicos presentes no sistema operacional e acessíveis pelas bibliotecas nativas da linguagem de programação podem ser usados para implementar a comunicação sobre uma arquitetura de rede baseada no padrão da Internet.