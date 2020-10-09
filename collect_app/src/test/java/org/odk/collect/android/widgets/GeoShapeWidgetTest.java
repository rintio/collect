package org.odk.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.interfaces.GeoButtonClickListener;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.assertGeoPolyBundleArgumentEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.stringFromDoubleList;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class GeoShapeWidgetTest {
    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final String answer = stringFromDoubleList();

    private WaitingForDataRegistry waitingForDataRegistry;
    private GeoButtonClickListener mockGeoButtonClickListener;
    private View.OnLongClickListener listener;

    @Before
    public void setup() {
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
        mockGeoButtonClickListener = mock(GeoButtonClickListener.class);
        listener = mock(View.OnLongClickListener.class);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        assertNull(widget.getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertEquals(widget.getAnswer().getDisplayText(), answer);
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_textViewDisplaysEmptyString() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        assertEquals(widget.binding.geoAnswerText.getText().toString(), "");
    }

    @Test
    public void whenPromptHasAnswer_textViewDisplaysAnswer() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertEquals(widget.binding.geoAnswerText.getText().toString(), answer);
    }

    @Test
    public void widgetCallsSetButtonLabelAndVisibility_whenPromptIsReadOnlyAndDoesNotHaveAnswer() {
        GeoShapeWidget widget = createWidget(promptWithReadOnly());
        verify(mockGeoButtonClickListener).setButtonLabelAndVisibility(widget.binding, true, false,
                R.string.geoshape_view_read_only, R.string.geoshape_view_change_location, R.string.get_shape);
    }

    @Test
    public void widgetCallsSetButtonLabelAndVisibility_whenPromptIsNotReadOnlyAndHasAnswer() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        verify(mockGeoButtonClickListener).setButtonLabelAndVisibility(widget.binding, false, true,
                R.string.geoshape_view_read_only, R.string.geoshape_view_change_location, R.string.get_shape);
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        widget.clearAnswer();

        assertEquals(widget.binding.geoAnswerText.getText(), "");
        verify(mockGeoButtonClickListener).setButtonLabelAndVisibility(widget.binding, false, false,
                R.string.geoshape_view_read_only, R.string.geoshape_view_change_location, R.string.get_shape);
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonAndAnswerTextViewForLong_callsLongClickListeners() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();
        widget.binding.geoAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
        verify(listener).onLongClick(widget.binding.geoAnswerText);
    }

    @Test
    public void setData_setsCorrectAnswerInAnswerTextView() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData(answer);
        assertEquals(widget.binding.geoAnswerText.getText().toString(), answer);
    }

    @Test
    public void setData_whenDataIsNotNull_updatesButtonLabel() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData(answer);
        verify(mockGeoButtonClickListener).setButtonLabelAndVisibility(widget.binding, false, true,
                R.string.geoshape_view_read_only, R.string.geoshape_view_change_location, R.string.get_shape);
    }

    @Test
    public void setData_whenDataIsNull_updatesButtonLabel() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        widget.setBinaryData("");
        verify(mockGeoButtonClickListener).setButtonLabelAndVisibility(widget.binding, false, false,
                R.string.geoshape_view_read_only, R.string.geoshape_view_change_location, R.string.get_shape);
    }

    @Test
    public void setData_callsValueChangeListener() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setBinaryData(answer);

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void whenPromptIsReadOnlyAndDoesNotHaveAnswer_bundleStoresCorrectValues() {
        GeoShapeWidget widget = createWidget(promptWithReadOnlyAndAnswer(null));
        widget.binding.simpleButton.performClick();
        assertGeoPolyBundleArgumentEquals(widget.bundle, "", GeoPolyActivity.OutputMode.GEOSHAPE, true);
    }

    @Test
    public void whenPromptIsNotReadOnlyAndHasAnswer_bundleStoresCorrectValues() {
        GeoShapeWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        widget.binding.simpleButton.performClick();
        assertGeoPolyBundleArgumentEquals(widget.bundle, answer, GeoPolyActivity.OutputMode.GEOSHAPE, false);
    }

    @Test
    public void buttonClick_callsOnButtonClicked() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        GeoShapeWidget widget = createWidget(prompt);

        widget.setPermissionUtils(permissionUtils);
        widget.binding.simpleButton.performClick();

        verify(mockGeoButtonClickListener).onButtonClicked(widget.getContext(), prompt.getIndex(), permissionUtils, null,
                waitingForDataRegistry, GeoPolyActivity.class, widget.bundle, GEOSHAPE_CAPTURE);
    }

    private GeoShapeWidget createWidget(FormEntryPrompt prompt) {
        return new GeoShapeWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"),
                waitingForDataRegistry, mockGeoButtonClickListener);
    }
}
