-- =====================================================
-- Add indexes to preco_ativo table for scanner performance
-- =====================================================

-- Index for findTopByAtivoIdOrderByDataHoraDesc queries
CREATE INDEX idx_preco_ativo_ativo_id_data_hora 
ON preco_ativo(ativo_id, data_hora DESC);

-- Additional index on ativo_id for general lookups
CREATE INDEX idx_preco_ativo_ativo_id 
ON preco_ativo(ativo_id);
