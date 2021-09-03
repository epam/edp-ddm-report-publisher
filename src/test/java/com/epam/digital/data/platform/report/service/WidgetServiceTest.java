package com.epam.digital.data.platform.report.service;

import static com.epam.digital.data.platform.report.util.TestUtils.dashboard;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.client.WidgetClient;
import com.epam.digital.data.platform.report.model.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class WidgetServiceTest {

    @Mock
    private WidgetClient widgetClient;

    private WidgetService instance;

    @BeforeEach
    void init() {
        instance = new WidgetService(widgetClient);
    }

    @Test
    void shouldSaveWidgets() {
        when(widgetClient.createWidget(any())).thenReturn(mockResponse(Widget.class));

        instance.save(dashboard("stub"));

        verify(widgetClient).createWidget(any());
    }

    private <T> ResponseEntity<T> mockResponse(Class<T> clazz) {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
