package lv.janis.bonussystem.model;

import jakarta.persistence.*;

@Entity
public class Assortment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // ROBOTS, CIRSANA, PILNIE, NEPILNIE
    private String type;

    // Only for NEPILNIE: I, II, III, IV
    private String gradeName;

    private boolean active = true;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}