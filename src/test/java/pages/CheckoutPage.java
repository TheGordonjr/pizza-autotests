package pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckoutPage {

    private final SelenideElement nameInput = $("#customer-name");
    private final SelenideElement emailInput = $("#customer-email");
    private final SelenideElement confirmOrderButton = $("#confirm-order");
    private final SelenideElement orderItemsBlock = $("#order-items");

    @Step("Проверить состав заказа перед подтверждением: '{pizzaName}' на сумму '{expectedTotal}'")
    public CheckoutPage verifyOrderSummary(String pizzaName, String expectedTotal) {
        orderItemsBlock.shouldHave(text(pizzaName));
        $("body").shouldHave(text("Итого: " + expectedTotal));
        return this;
    }

    @Step("Заполнить форму заказа: Имя = '{name}', Email = '{email}'")
    public CheckoutPage fillCustomerDetails(String name, String email) {
        nameInput.setValue(name);
        emailInput.setValue(email);
        return this;
    }

    @Step("Нажать 'Подтвердить заказ'")
    public CheckoutPage confirmOrder() {
        confirmOrderButton.click();
        return this;
    }

    @Step("Нажать 'Подтвердить заказ' с пустыми полями и проверить алерт '{expectedAlertText}'")
    public void confirmEmptyFormAndVerifyAlert(String expectedAlertText) {
        confirmOrderButton.click();
        Alert alert = Selenide.switchTo().alert();
        assertEquals(expectedAlertText, alert.getText());
        alert.accept();
    }

    @Step("Проверить блок успешного оформления заказа для клиента '{name}' ({email})")
    public CheckoutPage verifyOrderSuccessMessage(String name, String email, String pizzaName, String total) {
        // Проверяем все поля чека на экране подтверждения
        $("body").shouldHave(text("Заказ успешно оформлен!"));
        $("body").shouldHave(text("Имя: " + name)); // У меня не было требований для учебного ПО, поэтому не стал проверять какую-нибудь маску
        $("body").shouldHave(text("Email: " + email)); // То же самое. Можно вообще набор символов вводить. Если такой баг будет в требованиях, то можно сделать точную проверку
        $("body").shouldHave(text(pizzaName));
        $("body").shouldHave(text("Общая стоимость: " + total)); // Сверяем, что итоговая сумма в чеке совпадает с ценой пиццы, зафиксированной при добавлении в меню
        $("body").shouldHave(text("Статус: Ожидает подтверждения"));
        $("a.cart-link").shouldHave(text("0")); // после заказа корзина обнуляется
        return this;
    }

    @Step("Нажать кнопку 'Посмотреть мои заказы'")
    public OrdersPage goToMyOrdersFromConfirmation() {
        $(byText("Посмотреть мои заказы")).shouldBe(visible).click();
        return new OrdersPage();
    }
}