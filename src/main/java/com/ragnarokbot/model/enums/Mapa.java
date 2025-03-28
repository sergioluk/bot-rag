package com.ragnarokbot.model.enums;

public enum Mapa {
	BASE("malaya"),
	VALKIRIA("force_map1"),
    CHEFHARD1("bossnia_02"),
    CHEFHARD2("bossnia_07"),
    CHEFNORMAL1("bossnia_01"),
    CHEFNORMAL2("bossnia_08"),
    BIOHARD1("lhz_dun_n2"),
    BIOHARD2("lhz_dun_n5"),
    BIONORMAL1("lhz_dun_n"),
    BIONORMAL2("lhz_dun_n6"),
    OLDGH("1@gl_he"),
    TOMB("1@spa2");

    private final String nome;

    Mapa(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
