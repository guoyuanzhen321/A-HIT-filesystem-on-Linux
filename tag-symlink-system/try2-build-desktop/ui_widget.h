/********************************************************************************
** Form generated from reading UI file 'widget.ui'
**
** Created: Mon Jun 11 21:23:58 2012
**      by: Qt User Interface Compiler version 4.7.2
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_WIDGET_H
#define UI_WIDGET_H

#include <QtCore/QVariant>
#include <QtGui/QAction>
#include <QtGui/QApplication>
#include <QtGui/QButtonGroup>
#include <QtGui/QHeaderView>
#include <QtGui/QLabel>
#include <QtGui/QLineEdit>
#include <QtGui/QPushButton>
#include <QtGui/QTreeView>
#include <QtGui/QWidget>

QT_BEGIN_NAMESPACE

class Ui_Widget
{
public:
    QLabel *labelyuanmulu;
    QLabel *labelmubiaomulu;
    QLineEdit *lineEdityuanmulu;
    QLineEdit *lineEditmubiaomulu;
    QPushButton *pushButtonchyuanmulu;
    QPushButton *pushButtonchmubiaomulu;
    QPushButton *pushButtoncreate;
    QTreeView *treeView;
    QPushButton *pushButtonDelete;
    QPushButton *pushButtonChIDV3;
    QLabel *labelTitle;
    QLabel *labelArtist;
    QLabel *labelAlbum;
    QLabel *labelYear;
    QLabel *labelComment;
    QLineEdit *lineEditTitle;
    QLineEdit *lineEditArtist;
    QLineEdit *lineEditAlbum;
    QLineEdit *lineEditYear;
    QLineEdit *lineEditComment;
    QPushButton *pushButtonGetIDV3;
    QPushButton *pushButtonrefresh;
    QPushButton *pushButtoncopy;

    void setupUi(QWidget *Widget)
    {
        if (Widget->objectName().isEmpty())
            Widget->setObjectName(QString::fromUtf8("Widget"));
        Widget->resize(982, 579);
        labelyuanmulu = new QLabel(Widget);
        labelyuanmulu->setObjectName(QString::fromUtf8("labelyuanmulu"));
        labelyuanmulu->setGeometry(QRect(20, 30, 67, 17));
        labelmubiaomulu = new QLabel(Widget);
        labelmubiaomulu->setObjectName(QString::fromUtf8("labelmubiaomulu"));
        labelmubiaomulu->setGeometry(QRect(20, 70, 67, 17));
        lineEdityuanmulu = new QLineEdit(Widget);
        lineEdityuanmulu->setObjectName(QString::fromUtf8("lineEdityuanmulu"));
        lineEdityuanmulu->setGeometry(QRect(100, 20, 541, 27));
        lineEditmubiaomulu = new QLineEdit(Widget);
        lineEditmubiaomulu->setObjectName(QString::fromUtf8("lineEditmubiaomulu"));
        lineEditmubiaomulu->setGeometry(QRect(100, 60, 541, 27));
        pushButtonchyuanmulu = new QPushButton(Widget);
        pushButtonchyuanmulu->setObjectName(QString::fromUtf8("pushButtonchyuanmulu"));
        pushButtonchyuanmulu->setGeometry(QRect(660, 20, 95, 27));
        pushButtonchmubiaomulu = new QPushButton(Widget);
        pushButtonchmubiaomulu->setObjectName(QString::fromUtf8("pushButtonchmubiaomulu"));
        pushButtonchmubiaomulu->setGeometry(QRect(660, 60, 95, 27));
        pushButtoncreate = new QPushButton(Widget);
        pushButtoncreate->setObjectName(QString::fromUtf8("pushButtoncreate"));
        pushButtoncreate->setGeometry(QRect(770, 20, 61, 61));
        treeView = new QTreeView(Widget);
        treeView->setObjectName(QString::fromUtf8("treeView"));
        treeView->setGeometry(QRect(20, 110, 621, 461));
        pushButtonDelete = new QPushButton(Widget);
        pushButtonDelete->setObjectName(QString::fromUtf8("pushButtonDelete"));
        pushButtonDelete->setGeometry(QRect(880, 520, 91, 41));
        pushButtonChIDV3 = new QPushButton(Widget);
        pushButtonChIDV3->setObjectName(QString::fromUtf8("pushButtonChIDV3"));
        pushButtonChIDV3->setGeometry(QRect(770, 520, 101, 41));
        labelTitle = new QLabel(Widget);
        labelTitle->setObjectName(QString::fromUtf8("labelTitle"));
        labelTitle->setGeometry(QRect(660, 120, 71, 21));
        labelArtist = new QLabel(Widget);
        labelArtist->setObjectName(QString::fromUtf8("labelArtist"));
        labelArtist->setGeometry(QRect(660, 170, 71, 21));
        labelAlbum = new QLabel(Widget);
        labelAlbum->setObjectName(QString::fromUtf8("labelAlbum"));
        labelAlbum->setGeometry(QRect(660, 220, 71, 21));
        labelYear = new QLabel(Widget);
        labelYear->setObjectName(QString::fromUtf8("labelYear"));
        labelYear->setGeometry(QRect(660, 260, 71, 21));
        labelComment = new QLabel(Widget);
        labelComment->setObjectName(QString::fromUtf8("labelComment"));
        labelComment->setGeometry(QRect(660, 300, 81, 21));
        lineEditTitle = new QLineEdit(Widget);
        lineEditTitle->setObjectName(QString::fromUtf8("lineEditTitle"));
        lineEditTitle->setGeometry(QRect(720, 120, 251, 27));
        lineEditArtist = new QLineEdit(Widget);
        lineEditArtist->setObjectName(QString::fromUtf8("lineEditArtist"));
        lineEditArtist->setGeometry(QRect(720, 170, 251, 27));
        lineEditAlbum = new QLineEdit(Widget);
        lineEditAlbum->setObjectName(QString::fromUtf8("lineEditAlbum"));
        lineEditAlbum->setGeometry(QRect(720, 220, 251, 27));
        lineEditYear = new QLineEdit(Widget);
        lineEditYear->setObjectName(QString::fromUtf8("lineEditYear"));
        lineEditYear->setGeometry(QRect(720, 260, 251, 27));
        lineEditComment = new QLineEdit(Widget);
        lineEditComment->setObjectName(QString::fromUtf8("lineEditComment"));
        lineEditComment->setGeometry(QRect(660, 330, 311, 171));
        pushButtonGetIDV3 = new QPushButton(Widget);
        pushButtonGetIDV3->setObjectName(QString::fromUtf8("pushButtonGetIDV3"));
        pushButtonGetIDV3->setGeometry(QRect(660, 520, 101, 41));
        pushButtonrefresh = new QPushButton(Widget);
        pushButtonrefresh->setObjectName(QString::fromUtf8("pushButtonrefresh"));
        pushButtonrefresh->setGeometry(QRect(910, 20, 61, 61));
        pushButtoncopy = new QPushButton(Widget);
        pushButtoncopy->setObjectName(QString::fromUtf8("pushButtoncopy"));
        pushButtoncopy->setGeometry(QRect(840, 20, 61, 61));

        retranslateUi(Widget);

        QMetaObject::connectSlotsByName(Widget);
    } // setupUi

    void retranslateUi(QWidget *Widget)
    {
        Widget->setWindowTitle(QApplication::translate("Widget", "Qt-symlinktagsfs", 0, QApplication::UnicodeUTF8));
        labelyuanmulu->setText(QApplication::translate("Widget", "\346\272\220\347\233\256\345\275\225   \357\274\232", 0, QApplication::UnicodeUTF8));
        labelmubiaomulu->setText(QApplication::translate("Widget", "\347\233\256\346\240\207\347\233\256\345\275\225\357\274\232", 0, QApplication::UnicodeUTF8));
        pushButtonchyuanmulu->setText(QApplication::translate("Widget", "\351\200\211\346\213\251\346\272\220\347\233\256\345\275\225", 0, QApplication::UnicodeUTF8));
        pushButtonchmubiaomulu->setText(QApplication::translate("Widget", "\351\200\211\346\213\251\347\233\256\346\240\207\347\233\256\345\275\225", 0, QApplication::UnicodeUTF8));
        pushButtoncreate->setText(QApplication::translate("Widget", "\345\210\233\345\273\272", 0, QApplication::UnicodeUTF8));
        pushButtonDelete->setText(QApplication::translate("Widget", "\345\210\240\351\231\244\346\255\214\346\233\262", 0, QApplication::UnicodeUTF8));
        pushButtonChIDV3->setText(QApplication::translate("Widget", "\344\277\256\346\224\271IDV3\346\240\207\347\255\276", 0, QApplication::UnicodeUTF8));
        labelTitle->setText(QApplication::translate("Widget", "Title :", 0, QApplication::UnicodeUTF8));
        labelArtist->setText(QApplication::translate("Widget", "Artist :", 0, QApplication::UnicodeUTF8));
        labelAlbum->setText(QApplication::translate("Widget", " Album:", 0, QApplication::UnicodeUTF8));
        labelYear->setText(QApplication::translate("Widget", "Year :", 0, QApplication::UnicodeUTF8));
        labelComment->setText(QApplication::translate("Widget", "Comment :", 0, QApplication::UnicodeUTF8));
        pushButtonGetIDV3->setText(QApplication::translate("Widget", "\350\216\267\345\217\226IDV3\346\240\207\347\255\276", 0, QApplication::UnicodeUTF8));
        pushButtonrefresh->setText(QApplication::translate("Widget", "\345\210\267\346\226\260", 0, QApplication::UnicodeUTF8));
        pushButtoncopy->setText(QApplication::translate("Widget", "\345\244\215\345\210\266", 0, QApplication::UnicodeUTF8));
    } // retranslateUi

};

namespace Ui {
    class Widget: public Ui_Widget {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_WIDGET_H
