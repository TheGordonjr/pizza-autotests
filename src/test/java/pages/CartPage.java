package pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartPage {

    private final SelenideElement cartItemsContainer = $("#cart-items");
    private final SelenideElement clearCartButton = $("#clear-cart");
    private final SelenideElement placeOrderButton = $("#place-order");

    @Step("Проверить, что корзина пуста")
    public void verifyCartIsEmpty() {
        $("#cart-items > p").shouldBe(visible).shouldHave(text("Корзина пуста"));
        $("body").shouldHave(text("Итого: 0 ₽"));
    }

    @Step("Проверить наличие пиццы '{pizzaName}' и суммы '{expectedTotal}' в корзине")
    public void verifyItemAndTotal(String pizzaName, String expectedTotal) {
        cartItemsContainer.shouldHave(text(pizzaName));
        $("body").shouldHave(text("Итого: " + expectedTotal));
    }

    @Step("Очистить корзину")
    public CartPage clearCart() {
        clearCartButton.click();
        return this;
    }

    @Step("Нажать 'Оформить заказ' при непустой корзине")
    public CheckoutPage proceedToCheckout() {
        placeOrderButton.click();
        return new CheckoutPage();
    }

    @Step("Попытаться оформить пустую корзину и проверить предупреждение '{expectedAlertText}'")
    public void tryCheckoutEmptyCartAndVerifyAlert(String expectedAlertText) {
        placeOrderButton.click();
        Alert alert = Selenide.switchTo().alert(); // У пустой корзины вылезет системное окно, которое надо будет подтвердить
        assertEquals(expectedAlertText, alert.getText());
        alert.accept();
    }
}