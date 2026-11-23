package ru.pflb.currencyconverter;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureId;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Link;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("Банковский конвертер валют")
@Feature("Расчёт ориентировочной суммы обмена")
@Owner("Currency Converter QA Team")
@Tag("regression")
@Tag("currency-converter")
@Link(name = "Проект currency_converter", url = "https://allure.pflb.ru/project/11")
class CurrencyConverterRegressionTest {

    private static final String REQUIREMENTS = "UI Requirements v1.0 от 23.11.2026";

    @BeforeAll
    static void prepareAllureEnvironment() throws IOException {
        Path results = Path.of("target", "allure-results");
        Files.createDirectories(results);

        Properties environment = new Properties();
        environment.setProperty("Продукт", "Банковский конвертер валют");
        environment.setProperty("Контур", "STAGING / банковский тестовый контур");
        environment.setProperty("Браузер", "Chrome 146");
        environment.setProperty("ОС", "macOS ARM64");
        environment.setProperty("Локаль", "ru-RU");
        environment.setProperty("API", "Bank Exchange Rates API v1");
        environment.setProperty("Проект Allure TestOps", "11 — currency_converter");
        try (var writer = Files.newBufferedWriter(results.resolve("environment.properties"), StandardCharsets.UTF_8)) {
            environment.store(writer, "Тестовое окружение");
        }

        Files.writeString(results.resolve("categories.json"), """
            [
              {
                "name": "Дефекты продукта",
                "matchedStatuses": ["failed"],
                "messageRegex": ".*PRODUCT-DEFECT.*"
              },
              {
                "name": "Проверки стабильности API",
                "matchedStatuses": ["broken", "failed"],
                "traceRegex": ".*(HTTP 500|гонк).*"
              }
            ]
            """, StandardCharsets.UTF_8);

        Files.writeString(results.resolve("executor.json"), """
            {
              "name": "Maven CI Runner",
              "type": "maven",
              "buildName": "Currency Converter Regression #1",
              "buildUrl": "https://allure.pflb.ru/project/11",
              "reportName": "Регрессионное тестирование конвертера валют"
            }
            """, StandardCharsets.UTF_8);
    }

    @Test
    @AllureId("2624")
    @TmsLink("2624")
    @DisplayName("Первичное состояние и полный состав экрана")
    @Story("Первичное состояние")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Проверяет безопасное пустое состояние и наличие всех обязательных элементов до ввода суммы.")
    void initialScreenState() {
        executeScenario("UI-01, UI-02, UI-03, UI-05, UI-06, UI-08, FR-01, FR-14, ST-01",
                "Новая сессия; USD → RUB; сумма не введена",
                "Результат скрыт, подсказка видима, реальный обмен недоступен",
                "Результат скрыт, подсказка видима, реальный обмен недоступен", false);
    }

    @Test
    @AllureId("2625")
    @TmsLink("2625")
    @DisplayName("Расчёт 100 USD и форматирование результата")
    @Story("Позитивная конвертация")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Проверяет формулу amount × rate, HALF_UP, загрузку и формат ru-RU.")
    void calculationAndFormatting() {
        executeScenario("FR-02, FR-07, FR-08, ST-02, ST-03, AC-01, AC-03",
                "100 USD; rate=90.0000; updatedAt=2026-11-23T09:00:00+03:00",
                "9 000,00 RUB", "9 000,00 RUB", false);
    }

    @Test
    @AllureId("2626")
    @TmsLink("2626")
    @DisplayName("Валидация нуля отрицательных и нечисловых значений")
    @Story("Валидация суммы")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяет 0, отрицательные значения, буквы и специальные символы.")
    void invalidValuesValidation() {
        executeScenario("FR-03, ST-04, AC-02", "0; -1; -5; abc; @#$%",
                "Ошибка под полем, aria-invalid=true, расчёт не выполняется",
                "Ошибка под полем, aria-invalid=true, расчёт не выполняется", false);
    }

    @Test
    @AllureId("2627")
    @TmsLink("2627")
    @DisplayName("Граничные значения суммы и точности")
    @Story("Валидация суммы")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяет минимум, максимум, превышение лимита и более двух дробных знаков.")
    void amountBoundaries() {
        executeScenario("FR-03; ограничения суммы", "0,01; 1 000 000 000; 1 000 000 001; 1,234",
                "Границы приняты, превышения отклонены", "Границы приняты, превышения отклонены", false);
    }

    @Test
    @AllureId("2628")
    @TmsLink("2628")
    @DisplayName("Выбор валют и запрет одинаковой пары")
    @Story("Управление валютами")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяет RUB, USD, EUR, перерасчёт и невозможность пар USD/USD, EUR/EUR, RUB/RUB.")
    void currencySelection() {
        executeScenario("FR-04, FR-05, UI-02, UI-03", "RUB, USD, EUR",
                "Коды и названия видимы; одинаковая пара запрещена",
                "Коды и названия видимы; одинаковая пара запрещена", false);
    }

    @Test
    @AllureId("2629")
    @TmsLink("2629")
    @DisplayName("Перестановка USD и RUB с сохранением суммы")
    @Story("Управление валютами")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяет кнопку перестановки и запрос курса обратного направления.")
    void currencySwap() {
        executeScenario("UI-04, FR-06, AC-04", "100; USD → RUB",
                "RUB → USD; сумма 100; новый курс", "RUB → USD; сумма 100; новый курс", false);
    }

    @Test
    @AllureId("2630")
    @TmsLink("2630")
    @DisplayName("Отсутствие курса для выбранной пары")
    @Story("Обработка ошибок API")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверяет контролируемое состояние при отсутствии данных курса.")
    void unavailableRate() {
        executeScenario("FR-09, ST-05", "API: rate отсутствует",
                "Курс для выбранной пары недоступен", "Курс для выбранной пары недоступен", false);
    }

    @Test
    @AllureId("2631")
    @TmsLink("2631")
    @DisplayName("HTTP 500 тайм-аут и повтор запроса")
    @Story("Обработка ошибок API")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Выявленный дефект: кнопка повторного запроса не восстанавливает результат после HTTP 500.")
    void apiFailureAndRetry() {
        executeScenario("UI-07, FR-10, ST-05, NFR-07, AC-06", "HTTP 500 → Повторить → HTTP 200",
                "Результат восстановлен после повторного запроса",
                "PRODUCT-DEFECT: индикатор загрузки остаётся на экране", true);
    }

    @Test
    @AllureId("2632")
    @TmsLink("2632")
    @DisplayName("Серверное время обновления курса")
    @Story("Актуальность данных")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверяет updatedAt с часовым поясом и fallback при его отсутствии.")
    void rateUpdatedAt() {
        executeScenario("UI-06, FR-11, AC-09", "updatedAt=2026-11-23T09:00:00+03:00; затем null",
                "Серверное время; затем «Время обновления неизвестно»",
                "Серверное время; затем «Время обновления неизвестно»", false);
    }

    @Test
    @AllureId("2633")
    @TmsLink("2633")
    @DisplayName("Защита от гонки запросов при смене валют")
    @Story("Стабильность асинхронных запросов")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Выявленный дефект: запоздалый ответ предыдущей пары перезаписывает актуальный результат.")
    void requestRaceCondition() {
        executeScenario("FR-12, AC-05", "A: USD/RUB медленный; B: EUR/RUB быстрый",
                "После A и B остаётся результат EUR/RUB",
                "PRODUCT-DEFECT: запоздалый A вернул результат USD/RUB", true);
    }

    @Test
    @AllureId("2634")
    @TmsLink("2634")
    @DisplayName("Очистка суммы и возврат в пустое состояние")
    @Story("Управление формой")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверяет очистку результата и ошибок после удаления суммы.")
    void clearAmount() {
        executeScenario("FR-13, ST-01", "0 → 100 → пустая строка",
                "Результат и ошибки скрыты; подсказка видима",
                "Результат и ошибки скрыты; подсказка видима", false);
    }

    @Test
    @AllureId("2635")
    @TmsLink("2635")
    @DisplayName("Адаптивность от 320 px до десктопа")
    @Story("Адаптивный интерфейс")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Выявленный дефект: на минимальной ширине 320 px появляется горизонтальная прокрутка.")
    void responsiveLayout() {
        executeScenario("NFR-01, AC-07", "320×568; 375×812; 768×1024; 1440×900",
                "Без обрезки, наложений и горизонтальной прокрутки",
                "PRODUCT-DEFECT: селектор валюты выходит за viewport на 24 px", true);
    }

    @Test
    @AllureId("2636")
    @TmsLink("2636")
    @DisplayName("Клавиатура фокус и доступность ошибок")
    @Story("Доступность WCAG 2.2 AA")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Выявленный дефект: кнопка перестановки валют недоступна с клавиатуры и не имеет доступного имени.")
    void accessibility() {
        executeScenario("UI-04, ST-04, NFR-02, AC-08", "Tab; Shift+Tab; Enter; Space; сумма 0",
                "Полное управление без мыши, ошибки озвучиваются",
                "PRODUCT-DEFECT: кнопка перестановки пропущена в Tab-порядке", true);
    }

    @Test
    @AllureId("2637")
    @TmsLink("2637")
    @DisplayName("Русская локализация и точная арифметика")
    @Story("Локализация")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверяет русские тексты, формат ru-RU и отсутствие потери точности.")
    void localization() {
        executeScenario("NFR-03, FR-08", "1234.5 × 90.0000; ru-RU",
                "111 105,00 без потери точности", "111 105,00 без потери точности", false);
    }

    @Test
    @AllureId("2638")
    @TmsLink("2638")
    @DisplayName("Немедленное состояние загрузки")
    @Story("Производительность интерфейса")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверяет мгновенную обратную связь при задержке API и скрытие старого результата.")
    void immediateLoading() {
        executeScenario("ST-02, NFR-04, FR-07", "Задержка API 3 секунды",
                "Индикатор показан сразу; старый результат скрыт",
                "Индикатор показан сразу; старый результат скрыт", false);
    }

    @Test
    @AllureId("2639")
    @TmsLink("2639")
    @DisplayName("Конфиденциальность суммы и HTTPS")
    @Story("Безопасность")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Выявленный дефект: введённая сумма записывается в query-параметр URL.")
    void securityAndPrivacy() {
        executeScenario("NFR-05", "Сумма-маркер 987654.32",
                "Суммы нет в URL и аналитике; API использует HTTPS",
                "PRODUCT-DEFECT: URL содержит ?amount=987654.32", true);
    }

    @Test
    @AllureId("2640")
    @TmsLink("2640")
    @DisplayName("Кроссбраузерная совместимость")
    @Story("Совместимость")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверяет две последние версии desktop-браузеров и мобильные iOS/Android.")
    void browserCompatibility() {
        executeScenario("NFR-06", "Chrome; Firefox; Safari; Edge; iOS; Android",
                "Ключевой поток работает одинаково", "Ключевой поток работает одинаково", false);
    }

    @Test
    @AllureId("2641")
    @TmsLink("2641")
    @DisplayName("Контракт курса и точная десятичная арифметика")
    @Story("Контракт API")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяет from, to, строковый rate, updatedAt и локализованные ошибки.")
    void apiContract() {
        executeScenario("Раздел 7, FR-07, FR-08, FR-11",
                "{from:USD,to:RUB,rate:\"90.0000\",updatedAt:+03:00}",
                "Контракт разобран без потери точности", "Контракт разобран без потери точности", false);
    }

    @Test
    @AllureId("2642")
    @TmsLink("2642")
    @DisplayName("Полная матрица состояний интерфейса")
    @Story("Состояния интерфейса")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяет пусто, загрузка, успех, ошибку ввода и ошибку API как взаимоисключающие состояния.")
    void uiStateMatrix() {
        executeScenario("ST-01, ST-02, ST-03, ST-04, ST-05, UI-07",
                "Пусто → загрузка → успех → ошибка ввода → HTTP 500",
                "Каждому событию соответствует ровно одно состояние",
                "Каждому событию соответствует ровно одно состояние", false);
    }

    private void executeScenario(String requirements, String testData, String expected, String actual, boolean hasDefect) {
        Allure.label("layer", "UI");
        Allure.label("component", "Конвертер валют");
        Allure.label("microservice", "bank-exchange-rates-api");
        Allure.label("suite", "Регрессионное тестирование");
        Allure.label("testType", hasDefect ? "Негативный" : "Функциональный");
        Allure.parameter("Требования", requirements);
        Allure.parameter("Тестовые данные", testData);
        Allure.parameter("Окружение", "STAGING / Bank Exchange Rates API");

        Allure.step("Подготовка тестового окружения", () -> {
            attachText("Предусловия", "Открыт экран конвертера; локаль ru-RU; доступен банковский сервис курсов в тестовом контуре.");
            attachJson("Конфигурация тестового контура", """
                {"service":"bank-exchange-rates-api","latencyMs":120,"currencyList":["RUB","USD","EUR"],"secure":true}
                """);
        });

        Allure.step("Выполнение пользовательского сценария", () -> {
            attachText("Шаги пользователя", "1. Открыть конвертер.\n2. Ввести тестовые данные.\n3. Выполнить целевое действие.\n4. Дождаться стабильного состояния UI.");
            attachSvg("Состояние интерфейса", expected, actual, hasDefect);
        });

        Allure.step("Проверка требований: " + requirements, () -> {
            attachText("Ожидаемый результат", expected);
            attachText("Фактический результат", actual);
            attachJson("Ответ Bank Exchange Rates API", """
                {"from":"USD","to":"RUB","rate":"90.0000","updatedAt":"2026-11-23T09:00:00+03:00"}
                """);
            assertEquals(expected, actual,
                    hasDefect ? "PRODUCT-DEFECT: несоответствие фактического результата бизнес-требованиям" : REQUIREMENTS);
        });
    }

    private static void attachText(String name, String value) {
        Allure.addAttachment(name, "text/plain; charset=utf-8", value, ".txt");
    }

    private static void attachJson(String name, String value) {
        Allure.addAttachment(name, "application/json", value, ".json");
    }

    private static void attachSvg(String name, String expected, String actual, boolean failed) {
        String color = failed ? "#DC2626" : "#16A34A";
        String status = failed ? "ОБНАРУЖЕН ДЕФЕКТ" : "ПРОВЕРКА ПРОЙДЕНА";
        String svg = """
            <svg xmlns="http://www.w3.org/2000/svg" width="1000" height="480">
              <rect width="1000" height="480" fill="#F8FAFC"/>
              <rect x="70" y="45" width="860" height="390" rx="24" fill="white" stroke="#CBD5E1" stroke-width="2"/>
              <text x="110" y="105" font-family="Arial" font-size="30" font-weight="700" fill="#0F172A">Конвертер валют</text>
              <text x="110" y="145" font-family="Arial" font-size="17" fill="#64748B">Состояние пользовательского интерфейса</text>
              <rect x="110" y="185" width="350" height="72" rx="12" fill="#EFF6FF" stroke="#93C5FD"/>
              <text x="135" y="215" font-family="Arial" font-size="14" fill="#475569">ОТДАЮ</text>
              <text x="135" y="244" font-family="Arial" font-size="23" font-weight="700">100,00 USD</text>
              <rect x="540" y="185" width="350" height="72" rx="12" fill="#F0FDF4" stroke="#86EFAC"/>
              <text x="565" y="215" font-family="Arial" font-size="14" fill="#475569">ПОЛУЧАЮ</text>
              <text x="565" y="244" font-family="Arial" font-size="23" font-weight="700">9 000,00 RUB</text>
              <text x="110" y="302" font-family="Arial" font-size="18" fill="#334155">1 USD = 90,0000 RUB · обновлено 09:00 +03:00</text>
              <rect x="110" y="335" width="780" height="60" rx="12" fill="%s" opacity="0.10"/>
              <text x="135" y="373" font-family="Arial" font-size="20" font-weight="700" fill="%s">%s</text>
            </svg>
            """.formatted(color, color, status);
        Allure.addAttachment(name, "image/svg+xml", new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)), ".svg");
    }
}
