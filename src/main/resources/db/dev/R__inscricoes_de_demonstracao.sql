-- Dados fictícios
INSERT INTO inscricoes (tipo, nome, contato, canal, endereco, latitude, longitude, raio_metros, criada_em)
VALUES
    ('MORADOR', 'Morador Fictício Um', '11000000001', 'SMS',
     'Rua Fictícia das Águas, 120 - Bairro Exemplo', -23.55010, -46.64020, 300, CURRENT_TIMESTAMP),
    ('COMERCIANTE', 'Comércio Fictício Dois', '11000000002', 'WHATSAPP',
     'Rua Fictícia das Águas, 380 - Bairro Exemplo', -23.55130, -46.64100, 500, CURRENT_TIMESTAMP),
    ('MORADOR', 'Moradora Fictícia Três', 'moradora.tres@exemplo.com', 'EMAIL',
     'Rua Demonstração, 60 - Vila Teste', -23.56010, -46.64790, 300, CURRENT_TIMESTAMP)
ON CONFLICT (canal, contato) DO NOTHING;
