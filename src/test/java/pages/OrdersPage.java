package pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrdersPage {

    private final SelenideElement ordersList = $("#orders-list");
    private final SelenideElement cancelOrderButton = $("#orders-list > div > button"); // Красная кнопка "Отменить заказ"

    // Здесь ничего не всплывает, поэтому проверяем через текст содержимое истории заказов
    @Step("Проверить, что список заказов пуст")
    public void verifyNoOrders() {
        $("#orders-list > p").shouldBe(visible).shouldHave(text("У вас пока нет заказов"));
    }

    @Step("Проверить отображение заказа в истории: клиент '{customerName}', пицца '{pizzaName}', сумма '{expectedTotal}'")
    public void verifyOrderInHistory(String customerName, String pizzaName, String expectedTotal) {
        ordersList.shouldBe(visible);
        ordersList.shouldHave(text(customerName));
        ordersList.shouldHave(text(pizzaName));
        ordersList.shouldHave(text(expectedTotal));
    }

    // Раз есть кнопка отмены, почему бы и её не проверить
    @Step("Нажать 'Отменить заказ' и отказаться в окне подтверждения (нажать 'Отмена')")
    public OrdersPage clickCancelAndDismiss() {
        cancelOrderButton.shouldBe(visible).click();
        Alert confirm = Selenide.switchTo().alert();
        assertEquals("Вы уверены, что хотите отменить этот заказ?", confirm.getText());
        confirm.dismiss(); // Нажимаем "Отмена" в диалоговом окне браузера
        return this;
    }

    @Step("Нажать 'Отменить заказ' и подтвердить отмену (нажать 'OK')")
    public OrdersPage clickCancelAndAccept() {
        cancelOrderButton.shouldBe(visible).click();
        Alert confirm = Selenide.switchTo().alert();
        assertEquals("Вы уверены, что хотите отменить этот заказ?", confirm.getText());
        confirm.accept(); // Нажимаем "OK" в диалоговом окне браузера
        return this;
    }

    @Step("Проверить, что заказ отменен")
    public void verifyOrderCancelled() {
        // Проверяем либо исчезновение заказа (пустой список), либо смену статуса на "Отменен"
        if ($("#orders-list > p").exists()) {
            verifyNoOrders();
        } else {
            ordersList.shouldHave(text("Отменен"));
        }
    }
}