package com.comanda.comanda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.comanda.comanda.Model.Comanda;
import com.comanda.comanda.Model.ItemComanda;

class ComandaModelUnitTests {

    @Test
    void deveCalcularTotalERemoverItemPelosMetodosDaComanda() {
        Comanda comanda = new Comanda();
        ItemComanda primeiro = new ItemComanda(null, 3, new BigDecimal("2.35"), comanda);
        ItemComanda segundo = new ItemComanda(null, 2, new BigDecimal("4.10"), comanda);

        assertThat(comanda.calcularTotal()).isEqualByComparingTo("0");
        comanda.adicionarItem(primeiro);
        comanda.adicionarItem(segundo);
        assertThat(comanda.calcularTotal()).isEqualByComparingTo("15.25");

        comanda.removerItem(primeiro);
        assertThat(comanda.getItens()).containsExactly(segundo);
        assertThat(comanda.calcularTotal()).isEqualByComparingTo("8.20");
    }

    @Test
    void naoDevePermitirAlterarColecaoDiretamente() {
        Comanda comanda = new Comanda();
        comanda.adicionarItem(new ItemComanda(null, 1, BigDecimal.ONE, comanda));

        assertThatThrownBy(() -> comanda.getItens().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(comanda.getItens()).hasSize(1);
    }

    @Test
    void naoDeveAdicionarItemDeOutraComanda() {
        Comanda comanda = new Comanda();
        ItemComanda item = new ItemComanda(null, 1, BigDecimal.ONE, new Comanda());

        assertThatThrownBy(() -> comanda.adicionarItem(item))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(comanda.getItens()).isEmpty();
    }

    @Test
    void naoDeveAdicionarMesmoItemDuasVezes() {
        Comanda comanda = new Comanda();
        ItemComanda item = new ItemComanda(null, 2, BigDecimal.TEN, comanda);

        comanda.adicionarItem(item);
        comanda.adicionarItem(item);

        assertThat(comanda.getItens()).containsExactly(item);
        assertThat(comanda.calcularTotal()).isEqualByComparingTo("20");
    }

    @Test
    void naoDeveAdicionarItemNulo() {
        Comanda comanda = new Comanda();

        assertThatThrownBy(() -> comanda.adicionarItem(null))
                .isInstanceOf(NullPointerException.class);
        assertThat(comanda.getItens()).isEmpty();
    }
}
