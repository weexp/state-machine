package shop.behaviour;

import java.util.Set;

import com.github.davidmoten.fsm.example.generated.ProductBehaviourBase;
import com.github.davidmoten.fsm.example.generated.ProductStateMachine;
import com.github.davidmoten.fsm.example.shop.catalogproduct.CatalogProduct;
import com.github.davidmoten.fsm.example.shop.catalogproduct.event.ChangeProductDetails;
import com.github.davidmoten.fsm.example.shop.product.Product;
import com.github.davidmoten.fsm.example.shop.product.event.ChangeDetails;
import com.github.davidmoten.fsm.example.shop.product.event.Create;
import com.github.davidmoten.fsm.persistence.Entities;
import com.github.davidmoten.fsm.persistence.Persistence.EntityWithId;
import com.github.davidmoten.fsm.persistence.Property;
import com.github.davidmoten.fsm.runtime.Signaller;
import com.github.davidmoten.guavamini.Lists;

public final class ProductBehaviour extends ProductBehaviourBase<String> {

    @Override
    public ProductStateMachine<String> create(String id) {
        return ProductStateMachine.create(id, this);
    }

    @Override
    public Product onEntry_Created(Signaller<Product, String> signaller, String id, Create event, boolean replaying) {
        return new Product(event.productId, event.name, event.description);
    }

    @Override
    public Product onEntry_Changed(Signaller<Product, String> signaller, Product product, String id,
            ChangeDetails event, boolean replaying) {
        System.out.println(Entities.get().get(Product.class) + " products found");
        Set<EntityWithId<CatalogProduct>> set = Entities.get().get(CatalogProduct.class, //
                Lists.newArrayList(Property.create("productId", product.productId)));
        for (EntityWithId<CatalogProduct> cp : set) {
            signaller.signal(CatalogProduct.class, cp.id, new ChangeProductDetails(event.name, event.description));
        }
        return new Product(product.productId, event.name, event.description);
    }
}