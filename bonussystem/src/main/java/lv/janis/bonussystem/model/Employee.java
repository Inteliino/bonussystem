package lv.janis.bonussystem.model;

import jakarta.persistence.*;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String tabelesNr;

    private boolean active = true;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTabelesNr() {
        return tabelesNr;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTabelesNr(String tabelesNr) {
        this.tabelesNr = tabelesNr;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}