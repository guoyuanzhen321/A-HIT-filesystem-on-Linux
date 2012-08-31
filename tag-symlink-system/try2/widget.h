#ifndef WIDGET_H
#define WIDGET_H

#include <QWidget>

namespace Ui {
    class Widget;
}

class Widget : public QWidget
{
    Q_OBJECT

public:
    explicit Widget(QWidget *parent = 0);
    ~Widget();

private slots:
    void on_pushButtonchyuanmulu_clicked();

    void on_pushButtonchmubiaomulu_clicked();

    void on_pushButtoncreate_clicked();

    void on_pushButtonGetIDV3_clicked();

    void on_pushButtonDelete_clicked();

    void on_pushButtonrefresh_clicked();

    void on_pushButtonChIDV3_clicked();

    void on_pushButtoncopy_clicked();

private:
    Ui::Widget *ui;
};

#endif // WIDGET_H
