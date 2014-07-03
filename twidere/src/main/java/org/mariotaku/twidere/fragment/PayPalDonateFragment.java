/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountScreenName;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ParseUtils;

import java.text.NumberFormat;
import java.text.ParseException;

public class PayPalDonateFragment extends BaseFragment implements OnClickListener, TextWatcher {

	private EditText mEditName, mEditAmount;
	private Button mDonateButton;

	@Override
	public void afterTextChanged(final Editable s) {

	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mEditName.setText(getDefaultAccountScreenName(getActivity()));
		mEditAmount.addTextChangedListener(this);
		mDonateButton.setOnClickListener(this);
		mDonateButton.setEnabled(false);
	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.donate: {
				final String amount = ParseUtils.parseString(mEditAmount.getText());
				if (isEmpty(amount)) return;
				final String name = ParseUtils.parseString(mEditName.getText());
				final Uri.Builder builder = Uri.parse("https://www.paypal.com/cgi-bin/webscr").buildUpon();
				builder.appendQueryParameter("cmd", "_xclick");
				builder.appendQueryParameter("business", "mariotaku.lee@gmail.com");
				builder.appendQueryParameter("amount", amount);
				if (isEmpty(name)) {
					builder.appendQueryParameter("item_name", "Twidere donation");
				} else {
					builder.appendQueryParameter("item_name", String.format("Twidere donation by %s", name));
				}
				startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
				getActivity().onBackPressed();
				break;
			}
		}

	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_donate, container, false);
		mEditName = (EditText) view.findViewById(R.id.name);
		mEditAmount = (EditText) view.findViewById(R.id.amount);
		mDonateButton = (Button) view.findViewById(R.id.donate);
		return view;
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		if (isEmpty(s)) {
			mDonateButton.setEnabled(false);
			return;
		}
		final NumberFormat format = NumberFormat.getInstance(getResources().getConfiguration().locale);
		try {
			final Number number = format.parse(ParseUtils.parseString(s));
			mDonateButton.setEnabled(number.doubleValue() > 0);
		} catch (final ParseException e) {
			mDonateButton.setEnabled(false);
		}
	}
}
