package nss.mobile.video.ui.adapter;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import nss.mobile.video.R;

/**
 * 描述：
 * 邮箱 email:strive_bug@yeah.net
 * 创建时间 2018/10/30
 *
 * @author ql
 */
public class FunctionAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, QLViewHolder> {
    public static final int NORMAL_MENU = 2;

    public FunctionAdapter() {
        super(null);
        addItemType(NORMAL_MENU, R.layout.item_function_menu);
    }

    @Override
    protected void convert(QLViewHolder helper, MultiItemEntity item) {
        switch (item.getItemType()) {
            case NORMAL_MENU:
                convertNormal(helper, item);
                break;

        }
    }

    private void convertNormal(QLViewHolder helper, MultiItemEntity item) {
        NormalFunction nf = (NormalFunction) item;
        helper.setText(R.id.item_function_label_tv, nf.getMenuLabel())
                .setImageResource(R.id.item_function_icon_iv, nf.getMenuIcon());
    }

    public static class NormalFunction implements MultiItemEntity ,ITag{
        private CharSequence menuLabel;
        private int menuIcon;
        private int tag;

        public NormalFunction(CharSequence menuLabel, int menuIcon, int tag) {
            this.menuLabel = menuLabel;
            this.menuIcon = menuIcon;
            this.tag = tag;
        }

        public int getTag() {
            return tag;
        }

        public void setTag(int tag) {
            this.tag = tag;
        }

        public CharSequence getMenuLabel() {
            return menuLabel;
        }

        public void setMenuLabel(CharSequence menuLabel) {
            this.menuLabel = menuLabel;
        }

        public int getMenuIcon() {
            return menuIcon;
        }

        public void setMenuIcon(int menuIcon) {
            this.menuIcon = menuIcon;
        }

        @Override
        public int getItemType() {
            return NORMAL_MENU;
        }
    }
}
