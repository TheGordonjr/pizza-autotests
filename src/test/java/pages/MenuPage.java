package pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MenuPage {

    private final SelenideElement ordersLink = $("a.orders-link"); // Ссылка "Мои заказы"
    private final SelenideElement cartLink = $("a.cart-link"); // Ссылка "Корзина" со счетчиком товаров
    private final SelenideElement pizzaOfTheDayCard = $("#menu-container div.card.pizza-of-the-day"); // Карточка текущей "Пиццы дня"

    @Step("Открыть главную страницу пиццерии")
    public MenuPage openPage() {
        open("https://slqamsk.github.io/cases/pizza/v08/");
        return this;
    }

    @Step("Получить название текущей 'Пиццы дня'")
    public String getPizzaOfTheDayName() {
        String rawTitle = pizzaOfTheDayCard.$("h3").getText(); // Я решил брать заголовок третьего уровня
        return rawTitle.replace("(пицца дня)", "").trim(); // и убирать из него эту приписку
    }

    @Step("Проверить, что на 'Пиццу дня' корректно рассчитана скидка 10%") // В голову пришло только следующее
    public int verifyAndGetPizzaOfTheDayDiscountedPrice() {
        pizzaOfTheDayCard.shouldBe(visible); // Сначала определить всю визуальную часть
        pizzaOfTheDayCard.shouldHave(text("Пицца дня!"));
        pizzaOfTheDayCard.shouldHave(text("Скидка 10%"));

        String pizzaName = getPizzaOfTheDayName();
        $("body").shouldHave(text("Пицца дня: " + pizzaName + " со скидкой 10%!"));

        // Берем все числа больше 50, чтобы отсечь "10" из "Скидка 10%"
        String cardText = pizzaOfTheDayCard.getText();
        String[] rawNumbers = cardText.replaceAll("[^0-9]+", " ").trim().split("\\s+");

        java.util.List<Integer> prices = new java.util.ArrayList<>();
        for (String num : rawNumbers) {
            int val = Integer.parseInt(num);
            if (val > 50) {
                prices.add(val);
            }
        }

        // ну и вычисляем применение скидки
        assertTrue(prices.size() >= 2, "В карточке пиццы дня должно быть две цены (новая и старая), найдено: " + prices);
        int newPrice = Math.min(prices.get(0), prices.get(1));
        int oldPrice = Math.max(prices.get(0), prices.get(1));

        assertEquals(Math.round(oldPrice * 0.9), newPrice,
                "Скидка 10% рассчитана неверно! Старая цена: " + oldPrice + ", новая: " + newPrice);
        return newPrice;
    }

    @Step("Добавить 'Пиццу дня' в корзину")
    public MenuPage addPizzaOfTheDay() {
        pizzaOfTheDayCard.$("button").click();
        return this;
    }

    @Step("Добавить пиццу '{pizzaName}' в корзину и вернуть её актуальную цену")
    public int addPizzaByNameAndGetPrice(String pizzaName) {
        SelenideElement card = $("#menu-container").$(withText(pizzaName)).closest(".card");
        card.$("button").click();

        String cardText = card.getText();
        String[] rawNumbers = cardText.replaceAll("[^0-9]+", " ").trim().split("\\s+");
        java.util.List<Integer> prices = new java.util.ArrayList<>();
        for (String num : rawNumbers) {
            int val = Integer.parseInt(num);
            if (val > 50) {
                prices.add(val);
            }
        }

        if (card.has(cssClass("pizza-of-the-day")) && prices.size() >= 2) {
            return Math.min(prices.get(0), prices.get(1));
        }
        return prices.get(0);
    }

    @Step("Проверить, что недоступная пицца ('{pizzaName}') заблокирована для заказа") // На сайте недоступна только одна конкретная пицца (без рандома, как у пиццы дня. Если моих попыток хватило это заметить), поэтому решил проверить её по названию, что карточка помечена как unavailable, а кнопка заблокирована
    public void verifyPizzaIsUnavailable(String pizzaName) {
        SelenideElement card = $("#menu-container").$(withText(pizzaName)).closest(".card");
        card.shouldHave(cssClass("unavailable"));
        card.$("button").shouldHave(text("Не доступно")).shouldBe(disabled);
    }

    @Step("Проверить счетчик товаров в корзине: должно быть {expectedCount}")
    public void verifyCartBadgeCount(int expectedCount) {
        cartLink.shouldHave(text(String.valueOf(expectedCount)));
    }

    @Step("Перейти в корзину")
    public CartPage goToCart() {
        cartLink.click();
        return new CartPage();
    }

    @Step("Перейти в 'Мои заказы'")
    public OrdersPage goToOrders() {
        ordersLink.click();
        return new OrdersPage();
    }
}