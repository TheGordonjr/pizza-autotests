package tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Feature;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.CartPage;
import pages.CheckoutPage;
import pages.MenuPage;
import pages.OrdersPage;

@Feature("Автотесты пиццерии 'Итальянский уголок'")
public class PizzaTests {

    MenuPage menuPage = new MenuPage();

    @BeforeAll
    static void setupAll() {
        Configuration.browserSize = "1920x1080";
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(true)
                .savePageSource(false));
    }

    @BeforeEach
    void setUp() {
        menuPage.openPage();
        Selenide.localStorage().clear();
        Selenide.refresh();
    }

    @Test
    @DisplayName("1. Проверка пустой корзины и пустого списка заказов при первом входе")
    void emptyStateTest() {
        menuPage.verifyCartBadgeCount(0);

        CartPage cartPage = menuPage.goToCart();
        cartPage.verifyCartIsEmpty();

        OrdersPage ordersPage = menuPage.goToOrders();
        ordersPage.verifyNoOrders();
    }

    @Test
    @DisplayName("2. Проверка запрета добавления недоступной пиццы ('Трюфельная')")
    void unavailablePizzaTest() {
        menuPage.verifyPizzaIsUnavailable("Трюфельная");
        menuPage.verifyCartBadgeCount(0);
    }

    @Test
    @DisplayName("3. Проверка 'Пиццы дня' и применения скидки 10% в корзине")
    void pizzaOfTheDayDiscountTest() {
        // Определяем текущую "Пиццу дня" и проверяем расчет скидки 10% на главной странице
        String pizzaOfTheDayName = menuPage.getPizzaOfTheDayName();
        int discountedPrice = menuPage.verifyAndGetPizzaOfTheDayDiscountedPrice();

        // Добавляем "Пиццу дня" в корзину и проверяем, сохранилась ли скидка при переходе в корзину (Спойлер - нет. Так что просто в отчете отразится как падение)
        menuPage.addPizzaOfTheDay();
        menuPage.verifyCartBadgeCount(1);

        CartPage cartPage = menuPage.goToCart();
        cartPage.verifyItemAndTotal(pizzaOfTheDayName, discountedPrice + " ₽");
    }

    @Test
    @DisplayName("4. Добавление нескольких пицц, подсчет суммы и очистка корзины")
    void addMultipleAndClearCartTest() {
        int price1 = menuPage.addPizzaByNameAndGetPrice("Маргарита");
        int price2 = menuPage.addPizzaByNameAndGetPrice("Пепперони");
        int expectedTotal = price1 + price2;

        menuPage.verifyCartBadgeCount(2);

        CartPage cartPage = menuPage.goToCart();
        cartPage.verifyItemAndTotal("Маргарита", expectedTotal + " ₽");

        cartPage.clearCart();
        cartPage.verifyCartIsEmpty();
    }

    @Test
    @DisplayName("5. Негативный тест: попытка оформить заказ с пустой корзиной")
    void checkoutEmptyCartAlertTest() {
        CartPage cartPage = menuPage.goToCart();
        cartPage.tryCheckoutEmptyCartAndVerifyAlert("Корзина пуста!");
    }

    @Test
    @DisplayName("6. Негативный тест: попытка подтвердить заказ с пустыми полями имени и email")
    void checkoutEmptyFieldsValidationTest() {
        menuPage.addPizzaByNameAndGetPrice("Гавайская");

        CartPage cartPage = menuPage.goToCart();
        CheckoutPage checkoutPage = cartPage.proceedToCheckout();

        checkoutPage.confirmEmptyFormAndVerifyAlert("Пожалуйста, заполните все поля!");
    }

    @Test
    @DisplayName("7. Полный E2E сценарий: оформление заказа, проверка подтверждения и истории в 'Моих заказах'")
    void fullEndToEndOrderFlowTest() {
        int price = menuPage.addPizzaByNameAndGetPrice("Четыре сыра");
        String totalString = price + " ₽";

        CartPage cartPage = menuPage.goToCart();
        cartPage.verifyItemAndTotal("Четыре сыра", totalString);

        CheckoutPage checkoutPage = cartPage.proceedToCheckout();
        checkoutPage.verifyOrderSummary("Четыре сыра", totalString)
                .fillCustomerDetails("Иван Иванов", "ivan@example.com")
                .confirmOrder()
                .verifyOrderSuccessMessage("Иван Иванов", "ivan@example.com", "Четыре сыра", totalString);

        OrdersPage ordersPage = checkoutPage.goToMyOrdersFromConfirmation();
        ordersPage.verifyOrderInHistory("Иван Иванов", "Четыре сыра", totalString);
    }

    @Test
    @DisplayName("8. Отмена оформленного заказа: проверка отказа ('Отмена') и подтверждения ('OK')")
    void cancelOrderTest() {
        // Сначала оформляем тестовый заказ, чтобы было что отменять. Добавил конкретные ТД, чтобы тест не упал и не запутался
        int price = menuPage.addPizzaByNameAndGetPrice("Маргарита");
        String totalString = price + " ₽";

        CartPage cartPage = menuPage.goToCart();
        CheckoutPage checkoutPage = cartPage.proceedToCheckout();
        checkoutPage.fillCustomerDetails("Петр Петров", "petr@example.com")
                .confirmOrder()
                .verifyOrderSuccessMessage("Петр Петров", "petr@example.com", "Маргарита", totalString);

        // Переходим в "Мои заказы" и проверяем наличие заказа
        OrdersPage ordersPage = checkoutPage.goToMyOrdersFromConfirmation();
        ordersPage.verifyOrderInHistory("Петр Петров", "Маргарита", totalString);

        // 1. Нажимаем "Отменить заказ", но в окне выбираем "Отмена" — заказ должен остаться
        ordersPage.clickCancelAndDismiss()
                .verifyOrderInHistory("Петр Петров", "Маргарита", totalString);

        // 2. Нажимаем "Отменить заказ" и в окне нажимаем "OK" — заказ должен отмениться
        ordersPage.clickCancelAndAccept()
                .verifyOrderCancelled();
    }
}