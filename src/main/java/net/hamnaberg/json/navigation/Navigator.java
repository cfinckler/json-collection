package net.hamnaberg.json.navigation;

import net.hamnaberg.json.Collection;
import net.hamnaberg.json.Item;
import net.hamnaberg.json.Template;
import net.hamnaberg.json.util.Optional;

import java.net.URI;

public interface Navigator {
    Optional<Collection> follow(URI href);
    void create(URI href, Template template);
    void update(Item item);
}
