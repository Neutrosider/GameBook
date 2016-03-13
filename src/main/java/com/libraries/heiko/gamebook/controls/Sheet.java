package com.libraries.heiko.gamebook.controls;

import android.graphics.Paint;

import com.libraries.heiko.gamebook.GAnimation;
import com.libraries.heiko.gamebook.GameBook;
import com.libraries.heiko.gamebook.GameElement;
import com.libraries.heiko.gamebook.GamePage;

/**
 * Created by heiko on 22.02.2016.
 */
public class Sheet extends BaseElement
{
    public Sheet(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent)
    {
        super(a_id, a_page, a_book, a_parent);
        this.hideOverflow = true;
        this.animations.push(new GAnimation());
    }

    // Draws the Sheet on the framebuffer
    public void _Draw(float[] a_mvpMatrix)
    {
        this.DrawBasics(a_mvpMatrix);
    }
}
